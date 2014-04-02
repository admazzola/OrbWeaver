package com;

import java.util.List;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.network.IPChecker;
import com.network.Node;
import com.network.NodeInfo;
import com.network.NodeListMessage;
import com.orb.NodeFileChunkMessage;
import com.orb.NodeFileChunkRequestMessage;
import com.orb.NodeFileHashMessage;
import com.orb.QueuedChunkRequest;
import com.orb.WeaverOrb;

/*
 * This simple P2P single-file distribution library was written by Starflask (www.starflask.com)
 * 
 * 
 * 
 * 
 * 
 * 
 */

public class Weaver extends Thread {

	ServerSocket sSock;

	boolean isSeeding = false;
	// boolean isActive = false;

	int port = 32359;// default port

	// Seeder[] otherSeeders;
	// String[] otherLeechers;

	Node[] otherNodes = new Node[1000];

	String[] masterNodeAddresses = new String[100];

	Node myNode;

	// pass in addresses of master nodes so there is a starting point (starting
	// list)
	// master nodes must be running weaver
	public Weaver(String[] masterNodeAddresses) {
		this.setName("Weaver Thread");

		this.masterNodeAddresses = masterNodeAddresses;
		// maintains propogated connections with other people

		// seeds or leeches
	}

	// INPUTS - port to use, IP addresses of master nodes
	public Weaver(int port, String[] masterNodeAddresses) {
		this.setName("Weaver Thread");

		this.port = port;
		this.masterNodeAddresses = masterNodeAddresses;

	}

	// OUTPUTS 1 - list of all nodes, propagated
	// this is a possible result of the library! This is the whole bread and
	// butter
	public Node[] getNodes() {
		return otherNodes;
	}

	// OUTPUT 2
	public boolean hasFile() {
		return isSeeding;
	}
	
	public float getLeechProgress(){
		return getRegisteredOrb().getChunkManager().getChunkProgress();
	}
	
	

	@Override
	public void run() {
		System.out.println("Weaver started");

		try {
			startServer();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	// private final Object lock = new Object();
	final Lock lock = new ReentrantLock();

	public void update() throws Exception {

		updateNodes();

		Thread.sleep(3000);

	}

	/*
	 * Establish connections to the servers you learned about as a Client
	 */
	private void updateNodes() throws Exception {
		for (Node node : otherNodes) {
			if (node != null) {
				node.update();
			}

		}

	}

	int nodeCount = 0;

	private void startServer() throws Exception {

		myNode = new Node(getMyIPAddress(), port, this);
		// addNode(myServer);

		for (int i = 0; i < masterNodeAddresses.length; i++) {
			if (masterNodeAddresses[i] != null) {
				addNode(new Node(masterNodeAddresses[i], port, this));
			}
		}

		sSock = new ServerSocket(port);

		final Weaver weav = this;

		new Thread(new Runnable() {
			public void run() {
				while (true) {

					try {
						// keep adding nodes as they connect
						Socket clientSocket = sSock.accept();

						addNode(new Node(clientSocket, weav));

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}

		}).start();

		while (true) {

			try {
				update();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}

		}

	}

	/*
	 * This is an input!
	 */
	WeaverOrb registeredOrb = null;

	public void registerOrb(WeaverOrb orb) {
		registeredOrb = orb;
	}

	public WeaverOrb getRegisteredOrb() {
		return registeredOrb;
	}

	private String getMyIPAddress() throws Exception {

		return IPChecker.getIp();
	}

	private void addNode(Node newNode) {

		// lock.lock();
		// try{

		for (Node node : otherNodes) {
			if (node != null) {
				if (node.equals(newNode)) {
					System.out.println("node already exists!");
					return;
				}
			}
		}

		otherNodes[nodeCount] = newNode;
		nodeCount++;
		System.out.println("added a new node!! " + nodeCount);

		// }finally{
		// lock.unlock();
		// }

	}

	public NodeInfo[] getNodeInfo() {
		NodeInfo[] info = new NodeInfo[100];
		int nodeInfoCount = 0;

		for (Node node : otherNodes) {
			if (node != null && nodeInfoCount < info.length
					&& node.getNodeInfo() != null) {

				info[nodeInfoCount] = node.getNodeInfo();
				nodeInfoCount++;

			}
		}

		return info;
	}

	public void receiveNodeListMessage(NodeListMessage mess) { // threaded :o
																// watch
																// concurrency

		// do stuff;

		NodeInfo[] newNodes = mess.getNodeInfo();
		for (NodeInfo info : newNodes) {
			if (info != null) {
				addNode(new Node(info.getAddress(), info.getPort(), this));
			}
		}

	}

	public void receiveFileHashMessage(NodeFileHashMessage mess) {

		String hash = mess.getHash();

		getRegisteredOrb().setFileHash(hash);

	}

	public void receiveFileChunkMessage(
			NodeFileChunkMessage nodeFileChunkMessage) {
		if (!isSeeding) {
			this.getRegisteredOrb().getChunkManager()
					.receiveChunk(nodeFileChunkMessage.getChunk());
			System.out.println("Got file chunk");
		} else {
			System.err.println("Got file chunk, but I am a seeder!");
		}

	}

	List<QueuedChunkRequest> queuedChunkRequest = new ArrayList<QueuedChunkRequest>();
	final Lock chunkRequestQueueLock = new ReentrantLock();

	public void receiveFileChunkRequestMessage(
			NodeFileChunkRequestMessage nodeFileChunkRequestMessage) {
		if (isSeeding) {

			chunkRequestQueueLock.lock();
			try {
				queuedChunkRequest.add(new QueuedChunkRequest(
						nodeFileChunkRequestMessage.chunkId,
						nodeFileChunkRequestMessage.senderInfo));
			} finally {
				chunkRequestQueueLock.unlock();
			}

			System.out.println("Got file chunk request by "
					+ nodeFileChunkRequestMessage.senderInfo);

		} else {
			System.err.println("Got file chunk request, but I am a leecher!");
		}

	}

	public NodeInfo getMyNodeInfo() {
		return myNode.getNodeInfo();
	}

	public String[] getMasterAddresses() {
		return masterNodeAddresses;
	}

	public Node getMyNode() {
		return myNode;
	}

	public QueuedChunkRequest[] getQueuedChunkRequests() {
		QueuedChunkRequest[] array = null;

		if (queuedChunkRequest.isEmpty()) {
			return null;
		}

		chunkRequestQueueLock.lock();
		try {
			queuedChunkRequest.toArray(array);
		} finally {
			chunkRequestQueueLock.unlock();
		}

		return array;
	}

	public void clearChunkRequestQueue() {

		chunkRequestQueueLock.lock();
		try {
			queuedChunkRequest.clear();
		} finally {
			chunkRequestQueueLock.unlock();
		}

	}

}

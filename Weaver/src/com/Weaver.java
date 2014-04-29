package com;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
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
 * TODO: smarter node lists... include a 'last timestamp verified' to know when a node is too old 
 * -optimize
 * -make this work with multiple seeders, should DL faster for peers then
 * 
 * 
 */

public class Weaver extends Thread {
	
	public static final boolean USE_LOCAL_ADDRESS = true;

	ServerSocket sSock;

	boolean isSeeding = false;
	// boolean isActive = false;

	int port = 32359;// default port

	WeaverStatus currentStatus = WeaverStatus.INITIALIZING;
	int activeConnectionCount; //seeders or leechers interacting with
	// Seeder[] otherSeeders;
	// String[] otherLeechers;

	Node[] otherNodes = new Node[1000];

	NodeInfo[] masterNodeAddresses = new NodeInfo[100];

	Node myNode;

	// pass in addresses of master nodes so there is a starting point (starting
	// list)
	// master nodes must be running weaver
	public Weaver(NodeInfo[] masterNodeInfo) {
		this.setName("Weaver Thread");

		this.masterNodeAddresses = masterNodeInfo;
		// maintains propogated connections with other people

		// seeds or leeches
	}

	// INPUTS - port to use, IP addresses of master nodes
	public Weaver(NodeInfo[] masterNodeInfo,int port) {
		this.setName("Weaver Thread");

		this.port = port;
		this.masterNodeAddresses = masterNodeInfo;

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
	
	public float getLeechProgress() {
		try{
		return getRegisteredOrb().getChunkManager().getChunkProgress();
		}catch(Exception e){
			
		}
		
		return 0f;
	}
	
	public WeaverStatus getStatus(){
		return currentStatus;
	}
	
	public int getActiveConnectionCount(){
		return activeConnectionCount;
	}
	

	@Override
	public void run() {
		
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
			if (node != null ) {
				if(node.isActive()){
				node.update();
				}
			}

		}

	}

	int nodeCount = 0;

	private void startServer() throws Exception {
		
		

		myNode = new Node(getMyIPAddress(), port, this);
		// addNode(myServer);

		for (int i = 0; i < masterNodeAddresses.length; i++) {
			if (masterNodeAddresses[i] != null) {
				if(! myNode.getNodeInfo().equals( masterNodeAddresses[i] ) ){
				addNode(new Node(masterNodeAddresses[i], this),true);
				System.out.println("adding node from init args master list");
				}
			}
		}

		try{
		sSock = new ServerSocket(port);
		}catch(Exception e){
			System.err.println("port already bound!");
		}
		
		final Weaver weav = this;

		new Thread(new Runnable() {
			public void run() {
				while (true) {

					try {
						// keep adding nodes as they connect
						Socket clientSocket = sSock.accept();

						addNode(new Node(clientSocket, weav),true);
						System.out.println("adding node from connection");
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
		if(USE_LOCAL_ADDRESS){
		return InetAddress.getLocalHost().getHostAddress(); //local
		}else{
		return IPChecker.getIp();  //remote 
		}
		
	}

	private void addNode(Node newNode, boolean force) {

		// lock.lock();
		// try{

		for (int i=0;i < otherNodes.length; i++ ) {
			
			if (otherNodes[i] != null) {
				if (otherNodes[i].equals(newNode) ) {
					
					if(otherNodes[i].isActive()){
						System.out.println("node already exists and is active!");
					if(force){
						System.out.println("forcing node recreation");
						otherNodes[i] = newNode;//force creation of new node if they are directly contacting you
					}
					}else{
						System.out.println("rebuilding node");
						otherNodes[i] = newNode;
					}
					
					return;
					
				}
			}
		}

		otherNodes[nodeCount] = newNode;
		nodeCount++;
		System.out.println("added a new node!! " + nodeCount + newNode.getNodeInfo());

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
				addNode(new Node(info.getAddress(), info.getPort(), this),false);
				System.out.println("adding node from list message");
			}
		}

	}

	public void receiveFileHashMessage(NodeFileHashMessage mess) {


		getRegisteredOrb().setFileHash(mess.getHash());
		getRegisteredOrb().setTotalChunkCount(mess.getTotalChunkCount());
	}

	public void receiveFileChunkMessage(
			NodeFileChunkMessage nodeFileChunkMessage) {
		if (!isSeeding) {
			this.getRegisteredOrb().getChunkManager()
					.receiveChunk(nodeFileChunkMessage.getChunk());
			
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
					+ nodeFileChunkRequestMessage.senderInfo + " chunkId = " + nodeFileChunkRequestMessage.chunkId );

		} else {
			System.err.println("Got file chunk request, but I am a leecher!");
		}

	}

	public NodeInfo getMyNodeInfo() {
		return myNode.getNodeInfo();
	}

	public NodeInfo[] getMasterAddresses() {
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

		//I think im getting stuck in dead lock??
		chunkRequestQueueLock.lock();
		try {
			array = new QueuedChunkRequest[queuedChunkRequest.size()];
			queuedChunkRequest.toArray(array);//fill the array
			System.out.println(queuedChunkRequest.size());
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

	public void setIsSeeding(boolean isSeeding) {
		this.isSeeding = isSeeding;
	}
	
	public boolean getIsSeeding(){
		return isSeeding;
	}

	public void setStatus(WeaverStatus status) {
		this.currentStatus = status;
	}
	
	public static byte[] serialize(Object obj) throws IOException {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os = new ObjectOutputStream(out);
	    os.writeObject(obj);
	    return out.toByteArray();
	}

	public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
	    ByteArrayInputStream in = new ByteArrayInputStream(data);
	    ObjectInputStream is = new ObjectInputStream(in);
	    return is.readObject();
	}
	
}

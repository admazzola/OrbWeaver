package com.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.Weaver;
import com.orb.NodeFileChunkRequestMessage;
import com.orb.NodeFileHashMessage;

//only get hello message if debugging - sync issues ?


public class Node {
	
	PeerType peerType = PeerType.NONE;

	String address;// collected via communications, passed to others to
					// propogate connections
	
	int port;//their port
	
	long uniqueId;
	
	

	boolean active = true;

	private Socket socket;
	private  ObjectInputStream input = null;
	private  ObjectOutputStream output = null;
	
	
	Weaver weaver;
	

	public Node(Socket sock, Weaver weaver) {//I learned about this node because they connected to me
		this.weaver = weaver;
		
		try {
			this.socket = sock;
			input = new ObjectInputStream(socket.getInputStream());
			output = new ObjectOutputStream(socket.getOutputStream());
			System.out.println("New client connected to port " + sock.getPort());
			messageListener = new MessageListener(input,socket,this,weaver);
			
			this.port = sock.getPort();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	
	public Node(String addr, int port, Weaver weaver) {//I learned about this node via data transfer
		this.weaver = weaver;
		
		this.address = addr;
		this.port = port;
		
				
	}

	
	public Node(NodeInfo nodeInfo, Weaver weaver) {
		this.weaver = weaver;
		
		this.address = nodeInfo.getAddress();
		this.port = nodeInfo.getPort();
	}


	final Lock lock = new ReentrantLock();

	MessageListener messageListener;
	
	public void update() throws Exception {
		
		//System.out.println(address);
		
		
		
		if(socket == null){
			//If they never connected to me, connect from the other direction
			
			try{
				socket = new Socket(address,port);
			
			
				messageListener = new MessageListener(input,socket,this,weaver);
			
			
				output = new ObjectOutputStream(socket.getOutputStream());
			
			}catch(Exception e){
				System.err.println("could not connect to " + address + ":" + port);
			}
			
			//sendMessage(new NodeHelloMessage(weaver.getMyNodeInfo()) ); //send this at diff time?
			
		}else{
			
			System.out.println( "saying hello to node " + this );
			sendMessage(new NodeHelloMessage(weaver.getMyNodeInfo()) );//sends my info
			
			
			System.out.println( " sending lists to other node " + this );
			
			try {
				sendMessage(new NodeListMessage(weaver.getNodeInfo()) );  //sends info of all other nodes I know about
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
		//if I am connected, send my lists
		
		//if I am not connected, get connected
	
		
		
		
	}
	
	String lastSentHash = null;
	public void updateSeeding() throws Exception{//from orb
		//if(!isActive()){
		//	return;
		///}
		
		if(socket != null){
			
			if(weaver.getMyNode().isMasterNode() ){//if I am a master	
				String fileHash = weaver.getRegisteredOrb().getFileHash();

					if(fileHash!=null && !fileHash.equals(lastSentHash)){
						
						
						sendMessage(new NodeFileHashMessage(fileHash, weaver.getRegisteredOrb().getChunkManager().getTotalChunkCount() ) );
						lastSentHash = fileHash;
						
						//sends the correct file hash to others so they know to start seeding or leeching.
					}
					
			}
			
			
			
		}else{
			//this.setActive(false);
			System.err.println("cannot seed, no socket connection");			
		}
		
	}
	
	
	
	public void updateLeeching() throws Exception{//from orb
		//request chunks from seeders in list, they will respond with chunk
	
		int chunkId = weaver.getRegisteredOrb().getChunkManager().getNextLeechChunkId();
		if(socket != null){
			if(chunkId > -1){
				System.out.println( "requesting chunk"+chunkId+" from " + address + ":" + port);
				//ask this seeder node for the next needed chunk and send them my info
				sendMessage(new NodeFileChunkRequestMessage( chunkId, weaver.getMyNodeInfo() ) );
			}else{
				System.out.println("I have all chunks");
			}
		}
	}
	
	
	public void sendMessage(Message m) throws Exception {
		System.out.println("sending message "+m.getClass().getName());
			if(! weaver.getMyNodeInfo().equals(getNodeInfo()) ){
		
			
				
				output.writeObject(m);
				
		
			}
	}


	public boolean isActive() {
		return active;
	}


	public void setActive(boolean active) {
		this.active = active;
	}

	
	
	public NodeInfo getNodeInfo(){ //broken?
		if(address == null){
			return null;
		}
		
		return new NodeInfo(address,port);
		
	}
	


	public void receiveNodeHelloMessage(NodeHelloMessage mess) {//threaded , fix concurrency
		
		address = mess.nodeInfo.address;
		port = mess.nodeInfo.port;
		
		System.out.println(" got new node info... port and address: " + address + " " + port );
		
		
	}


	public String toString(){
		return address + ":" + port;
		
	}
	
	@Override
	public boolean equals(Object o){
		
		
		if(o instanceof Node){
			if(address==null || address.equals(((Node)o).address)){
				return true;
			}
		}
		
		return false;		
	}


	public boolean isMasterNode() {
		
		for(NodeInfo info: weaver.getMasterAddresses() ){
			//System.out.println(this.address + " " + this.port );
			if(this.address.equals(info.getAddress()) && this.port == info.getPort()){
				return true;
			}
			
		}
		
		return false;
	}


	
	

}

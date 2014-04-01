package com;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Weaver extends Thread{

	
	ServerSocket sSock;
	
	boolean isSeeding = false;
	boolean isActive = false;
	
	final int port = 32359;
	
	//Seeder[] otherSeeders;
	//String[] otherLeechers;
	
	Node[] otherNodes = new Node[1000];
	
	String[] masterNodeAddresses = new String[100];
	
	Node myNode;

	//pass in addresses of master nodes so there is a starting point (starting list)
	//master nodes must be running weaver
	public Weaver(String[] masterNodeAddresses){
		
		this.masterNodeAddresses = masterNodeAddresses;
		//maintains propogated connections with other people
		
		//seeds or leeches
		
		
		
	}
	
	//this is the end result of the library! This is the whole bread and butter
	public Node[] getNodes(){
		return otherNodes;
	}
	
	
	
	@Override
	public void run(){
		System.out.println("Weaver started");
		
		try {
			startServer();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
		
		
		
	}
	

	//private final Object lock = new Object();
	final Lock lock = new ReentrantLock();
	
	public void update() throws Exception{		
		
		
			

			
				updateNodes();
			
					
				Thread.sleep(500);
			
		
		
	}

	

	/*
	 * Establish connections to the servers you learned about as a Client
	 *  
	 */
	private void updateNodes() throws Exception{
		for(Node node : otherNodes){
			if(node!=null){
				node.update(this);
			}
			
		}
		
		
	}
	
	


	int nodeCount = 0;
	private void startServer() throws Exception {		
		
		myNode = new Node(getMyIPAddress() , port);
		//addNode(myServer);
		
		
		for(int i = 0; i < masterNodeAddresses.length; i++){
			if(masterNodeAddresses[i] !=null){
				addNode(new Node(masterNodeAddresses[i] , port ));
			}
		}
		
		sSock = new ServerSocket(port);
		
		new Thread(    new Runnable() 
	    {
	        public void run() 
	        {
	        	while(true){
	    			
	        		try {
	        			//keep adding nodes as they connect
						Socket clientSocket = sSock.accept();
						
						addNode( new Node(clientSocket) );
						
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		
	    		}
	        }

			
	    }).start();
		
		while(true){
			
			try {
				update();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
			
			
		}
		
		
	}
	
	
	private String getMyIPAddress() throws Exception{
		
		return IPChecker.getIp();
	}

	
	
	
	private void addNode(Node newNode) {
		
		
		//lock.lock();
		//try{
		
			for(Node node: otherNodes){
				if(node !=null ){
					if(node.equals(newNode)){
						System.out.println("node already exists!");
						return;
					}					
				}
			}
			
			otherNodes[nodeCount] = newNode;						
			nodeCount++;
			System.out.println( "added a new node!! " + nodeCount);
			
		//}finally{
		//	lock.unlock();
		//}

				
	}

	
	
	
	public NodeInfo[] getNodeInfo() {
		NodeInfo[] info = new NodeInfo[100];
		int nodeInfoCount = 0;
		
		for(Node node : otherNodes)	{
			if(node!=null && nodeInfoCount < info.length && node.getNodeInfo()!=null){				
				
				info[nodeInfoCount] = node.getNodeInfo();
				nodeInfoCount++;
				
			}			
		}				
		
		return info;
	}

	
	
	public void receiveNodeListMessage(NodeListMessage mess) { // threaded :o  watch concurrency
		
			//do stuff;
			
			NodeInfo[] newNodes = mess.nodeInfo;
			for(NodeInfo info : newNodes){
				if(info!=null){
				addNode(new Node(info.address, info.port));
				}
			}
			
			
		

	}

	public NodeInfo getMyNodeInfo() {
		return myNode.getNodeInfo();
	}
	
	
}

package com;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//only get hello message if debugging - sync issues ?


public class Node {
	
	PeerType peerType = PeerType.NONE;

	String address;// collected via communications, passed to others to
					// propogate connections
	
	int port;//their port
	
	long uniqueId;

	private Socket socket;
	private  ObjectInputStream input = null;
	private  ObjectOutputStream output = null;
	
	
	
	

	public Node(Socket sock) {//I learned about this node because they connected to me
		try {
			this.socket = sock;
			input = new ObjectInputStream(socket.getInputStream());
			output = new ObjectOutputStream(socket.getOutputStream());
			System.out.println("New client connected to port " + sock.getPort());
			
			this.port = sock.getPort();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public Node(String addr, int port) {//I learned about this node via data transfer
		this.address = addr;
		this.port = port;
		
		System.out.println( "addr " + addr);
		
	}

	
	final Lock lock = new ReentrantLock();

	MessageListener messageListener;
	public  void update(Weaver weaver) throws Exception {
		
		//System.out.println(address);
	
		
		
		if(socket == null){
			//If they never connected to me, connect from the other direction
			
			socket = new Socket(address,port);
			
			
			messageListener = new MessageListener(input,socket,this,weaver);
			
			output = new ObjectOutputStream(socket.getOutputStream());
			
			
			//sendMessage(new NodeHelloMessage(weaver.getMyNodeInfo()) ); //send this at diff time?
			
		}else{
			
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
	
	private void sendMessage(Message m) throws Exception {
		
			output.writeObject(m);
		
			
		
	}


	NodeInfo getNodeInfo(){
		if(address == null){
			return null;
		}
		
		return new NodeInfo(address,port);
		
	}
	


	public void receiveNodeHelloMessage(NodeHelloMessage mess) {//threaded , fix concurrency
		
		address = mess.nodeInfo.address;
		port = mess.nodeInfo.port;
		
		System.out.println(" got new port and address " + address + " " + port );
		
		
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
	

}

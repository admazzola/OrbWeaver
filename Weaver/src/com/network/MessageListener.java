package com.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.Weaver;
import com.orb.NodeFileChunkMessage;
import com.orb.NodeFileChunkRequestMessage;
import com.orb.NodeFileHashMessage;

public class MessageListener extends Thread{

	ObjectInputStream input;
	Socket socket;
	Weaver weaver;
	Node node;
	
	public MessageListener(ObjectInputStream input, Socket socket, Node node, Weaver weaver) {
		this.input=input;
		this.socket=socket;
		this.weaver = weaver;
		this.node = node;
		start();
		
		
	}

	
	public boolean active = true;
	
	final Lock processMessageLock = new ReentrantLock();
	@Override
	public void run(){
		
		if(input == null){ //messy but whatever
		
		try {
			input = new ObjectInputStream(socket.getInputStream()); //waits until the remote output connected up
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		}
		
		
		while(node.isActive() ){
			
			try {				
			processMessage(input);
			} catch (Exception e) {	
				node.setActive(false);
				System.err.println("deactivating stream from "+ node.getNodeInfo());
				//e.printStackTrace();
			}
			
			
		}
	}

	
	
	private void processMessage(ObjectInputStream o) throws Exception{
			
		
		
		Object obj = o.readObject(); //waits until an object is written
		System.out.println("Received Message " + obj);
		
		if(obj instanceof Message){
			
			if(obj instanceof NodeHelloMessage){
				System.out.println( " got node hello message ");
				
				node.receiveNodeHelloMessage( ((NodeHelloMessage)obj) );  //threaded :o	
				
			}	
			
			if(obj instanceof NodeListMessage){
				System.out.println( " got node list message ");
				
				weaver.receiveNodeListMessage(((NodeListMessage)obj) );  //threaded :o	
				
			}	
			
			if(obj instanceof NodeFileHashMessage && node.isMasterNode()){//only accept from masters to avoid attacks
				System.out.println( " got file hash message from a hardcoded master ");
				
				weaver.receiveFileHashMessage(((NodeFileHashMessage)obj) );  //threaded :o	
				
			}
			
			
			if(obj instanceof NodeFileChunkRequestMessage  ){//ask for pieces of the file ...from leecher to seeder
				
				System.out.println( " got file chunk request message ");
				
				weaver.receiveFileChunkRequestMessage(((NodeFileChunkRequestMessage)obj) );  //threaded :o	
				
				
			}


			
			
			
		
		}
		

	
	}
	
	
	
	
}

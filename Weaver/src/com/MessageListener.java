package com;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	
	@Override
	public void run(){
		try {
			input = new ObjectInputStream(socket.getInputStream()); //waits until the remote output connected up
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		
		
		while(active){
			
			try {
			processMessage(input);
			} catch (Exception e) {				
				e.printStackTrace();
			}	
			
			
		}
	}

	//final Lock lock = new ReentrantLock();
	
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
		
		}
		

	
	}
	
	
	
	
}

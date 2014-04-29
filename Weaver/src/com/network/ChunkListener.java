package com.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.Weaver;
import com.orb.ChunkManager;
import com.orb.NodeFileChunkMessage;
import com.orb.NodeFileChunkRequestMessage;
import com.orb.NodeFileHashMessage;

public class ChunkListener extends Thread{

	
	DatagramSocket socket;
	Weaver weaver;
	Node node;
	
	public ChunkListener( DatagramSocket socket, Node node, Weaver weaver) {
		
		this.socket=socket;
		this.weaver = weaver;
		this.node = node;
		start();
		
		
	}

	
	public boolean active = true;
	
	final Lock processMessageLock = new ReentrantLock();
	@Override
	public void run(){
		
		
		
		
		while(node.isActive() ){
			
			try {				
			processMessage();
			} catch (Exception e) {	
				node.setActive(false);
				System.err.println("deactivating stream from "+ node.getNodeInfo());
				//e.printStackTrace();
			}
			
			
		}
	}

	
	
	private void processMessage() throws Exception{
		byte[] data = new byte[ChunkManager.CHUNK_LENGTH+1000];
		DatagramPacket packet = new DatagramPacket(data,data.length);
		socket.receive(packet);
		
		Object obj = Weaver.deserialize(packet.getData());
				
		System.out.println("Received Message " + obj);
		
		if(obj instanceof Message){
			

			
			if(obj instanceof NodeFileChunkMessage  ){//ask for pieces of the file ...from leecher to seeder
				
				System.out.println( " got file chunk message ");
				
				weaver.receiveFileChunkMessage(((NodeFileChunkMessage)obj) );  //threaded :o	
				
				
			}


			
			
			
		
		}
		

	
	}
	
	
	
	
}

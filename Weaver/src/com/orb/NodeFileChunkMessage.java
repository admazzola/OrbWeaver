package com.orb;

import java.io.Serializable;

import com.network.Message;



public class NodeFileChunkMessage extends Message implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 176633570L;
	
	public NodeFileChunkMessage(){
		
		
	}
	
	
	Chunk chunk;
	
	
	public NodeFileChunkMessage(Chunk chunk){
		this.chunk=chunk;
		setReliable(false);
	}


	public Chunk getChunk() {
		// TODO Auto-generated method stub
		return chunk;
	}
	
	
	

}

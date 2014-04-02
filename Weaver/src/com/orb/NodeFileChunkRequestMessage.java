package com.orb;

import java.io.Serializable;

import com.network.Message;
import com.network.NodeInfo;



public class NodeFileChunkRequestMessage extends Message implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 17663513570L;
	
	public NodeFileChunkRequestMessage(){
		
		
	}
	
	
	public int chunkId;
	public NodeInfo senderInfo;
	
	public NodeFileChunkRequestMessage(int id, NodeInfo info){
		chunkId=id;
		senderInfo = info;
	}
	
	
	

}

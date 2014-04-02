package com.network;

import java.io.Serializable;



public class NodeListMessage extends Message implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 176457347904580470L;
	
	public NodeListMessage(){
		
		
	}
	
	
	NodeInfo nodeInfo[];
	
	public NodeListMessage(NodeInfo[] nodeInfo){
		this.nodeInfo=nodeInfo;
	}

	public NodeInfo[] getNodeInfo() {
		return nodeInfo;
	}
	
	
	

}

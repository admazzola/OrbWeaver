package com.network;

import java.io.Serializable;

public class NodeInfo implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 263150310149L;
	
	
	
	String address;
	int port;
	
	
	public NodeInfo(String address, int port) {
		this.address=address;
		this.port=port;
	}


	public String getAddress() {	
		return address;
	}


	public int getPort() {
		
		return port;
	}
}

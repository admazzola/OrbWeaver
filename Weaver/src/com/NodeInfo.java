package com;

import java.io.Serializable;

public class NodeInfo implements Serializable {

	
	String address;
	int port;
	
	
	public NodeInfo(String address, int port) {
		this.address=address;
		this.port=port;
	}
}

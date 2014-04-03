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
	
	@Override
	public boolean equals(Object o){
		if(o instanceof NodeInfo){
			if(((NodeInfo)o).address!=null && ((NodeInfo)o).address.equals(address) && ((NodeInfo)o).port == port ){
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public String toString(){
		return "NodeInfo: address = " + address + ",port = " + port;
	}
	
}

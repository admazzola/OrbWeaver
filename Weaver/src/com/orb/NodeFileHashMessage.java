package com.orb;

import java.io.Serializable;

import com.network.Message;

/*Sent from the master nodes to other nodes to let them know what the SHA256
 * hash of the correct file should be.  This then lets nodes determine if they 
 * should be a leecher or a seeder.
 * 
 * 
 */


public class NodeFileHashMessage extends Message implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 176325131570L;
	
	public NodeFileHashMessage(){
		
		
	}
	
	
	String hash;
	
	public NodeFileHashMessage(String hash){
		this.hash = hash;
	}

	public String getHash() {
		// TODO Auto-generated method stub
		return hash;
	}
	
	
	

}

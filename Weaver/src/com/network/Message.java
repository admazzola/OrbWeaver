package com.network;

import java.io.Serializable;

public class Message implements Serializable{
	
	
	private static final long serialVersionUID = 111111111L;

	public Message(){
		
		
	}

	boolean reliable = true;
	public boolean isReliable() {
		return reliable;
	}
	public void setReliable(boolean reliable) {
		this.reliable = reliable;
	}
}

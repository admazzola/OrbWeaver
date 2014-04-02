package com.orb;

import java.io.Serializable;

public class Chunk implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 66756628L;

	byte[] data;
	int id;//placement in the while file
	
	public Chunk(int id, byte[] data) {
		this.data = data;
		this.id=id;
	}
	
	
	@Override
	public Object clone(){
		return new Chunk(id,data.clone());
	}


	public byte[] getData() {
		// TODO Auto-generated method stub
		return data;
	}
	
}

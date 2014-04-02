package com.orb;

import java.io.File;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;




public class VersionManager {

	//private boolean fileUpToDate = false;
	WeaverOrb weaverOrb;
	
	
	public VersionManager(WeaverOrb weaverOrb){
		this.weaverOrb=weaverOrb;
		
				
	}
	
	
	/*
	 * 
	 * if(hasCurrentVersion()){
			fileUpToDate = true;
			
			//start seeding
			
			
		}else{
			//start leeching
			1. ask permission to overwrite old jar
			 * 2. find peeps who have the file
			 * 3. get it from them in tiny packets
			 
			
		}
	 */

	public boolean checkVersion(String currentHash) {
		//String currentHash = null;
		String gameJarHash = null;
		
	
		try {	
			gameJarHash = getGameJarHash();			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	/*	try {
			currentHash = Http.performGet(new URL( weaverOrb. ));
		} catch (Exception e) {			
			e.printStackTrace();
		}*/
		
		if(gameJarHash!=null && currentHash!=null){
			if(currentHash.startsWith(gameJarHash) && gameJarHash.length() >= 40){
				return true;				
			}
		}		

		return false;
	}
	
	
	
	
	String getGameJarHash() throws Exception{		
		File file = new File(  weaverOrb.getFilePath() );		
		HashCode hc = Files.hash(file, Hashing.sha1());
		return hc.toString();
	}
	
	
	
}
package com.orb;

import com.Weaver;
import com.network.Node;
import com.network.NodeInfo;

/*
 * An orb is a handler for a file that should be duplicated amongst all nodes.
 * Nodes that run Orb who have the file will try to seed it to the other nodes.
 * Nodes that run Orb that don't have the file will try to download it from the other nodes.
 * 
 * Orb determines whether or not you have a file by checking at the designated filePath and comparing the
 * [SHA-256] hash of that file with the hash of the file on the master nodes.  The master nodes need to have
 * the file and need to be running Orb. 
 */
public class WeaverOrb extends Thread {

	String filePath = null;

	String fileHash = null;

	Weaver weaver;
	
	ChunkManager chunkManager;

	public WeaverOrb(String path, Weaver weaver) {
		this.setName("Orb Thread");
		
		filePath = path;
		this.weaver = weaver;
		
		
	}

	@Override
	public void start() {
		super.start();
		
	}

	@Override
	public void run() {
		


		initialize();

		try {

			while (true) {
				update();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	VersionManager versionManager;

	private void initialize() {
		chunkManager = new ChunkManager(this);
		versionManager = new VersionManager(this);

	}

	boolean shouldCheckVersion = true;
	//boolean seeding = false;

	public void update() throws Exception {

		if (fileHash != null) {
			if (shouldCheckVersion) {

				boolean hasCurrentVersion = versionManager.checkVersion(fileHash);
				
				
				shouldCheckVersion = false;

				weaver.setIsSeeding(hasCurrentVersion); // if my files hash matches the hash
											// given by a master node, seed to
											// others
				System.out.println( " seeding? " + weaver.getIsSeeding() );
				
				if(  weaver.getIsSeeding() ){
					//init seeding
					getChunkManager().generateChunksFromFile();
				}

			} else {
				
				

				if ( weaver.getIsSeeding() ) {

					for (Node node : weaver.getNodes()) {
						if (node != null) {
							node.updateSeeding();
						}

					}
					
					
					if(weaver.getQueuedChunkRequests()!=null){
						System.out.println( " have a queued chunk request ");
					for( QueuedChunkRequest qcr :   weaver.getQueuedChunkRequests()){
						if(qcr != null && getNodeFromInfo(qcr.senderInfo)!=null ){		
											
							getNodeFromInfo(qcr.senderInfo).sendMessage(new NodeFileChunkMessage( weaver.getRegisteredOrb().getChunkManager().getChunkFromId(qcr.chunkId) ) );
						}else{
							System.err.println("got chunk request but cant locate node to sent it to");
						}
					}
					}
					
					weaver.clearChunkRequestQueue();
										

				}else{
					
					
					for (Node node : weaver.getNodes()) {
						if (node != null) {
							node.updateLeeching();
						}

					}
					
					
					chunkManager.updateLeeching();
					//shouldCheckVersion = true; //keep checking version as you update leeching until you become a seeder
					
				}

			}

		} else {
			if (weaver.getMyNode()!=null && weaver.getMyNode().isMasterNode()) {// if I am a master
				
				if(weaver.getRegisteredOrb()!=null){
					weaver.getRegisteredOrb().readFileHashAsMaster();
				}else{
					System.err.println("no registered orb");
				}

			}
			
			
			/*//request unique file hash
			for (Node node : weaver.getNodes()) {
				if (node != null) {
					node.update();
				}
			}*/
				System.out.println("waiting for unique file hash from Master Node with file");

			
		}

		Thread.sleep(500);

	}

	private Node getNodeFromInfo(NodeInfo senderInfo) {
		
		for(Node node : weaver.getNodes()){
			
			if(node!=null && senderInfo!=null && senderInfo.equals(node.getNodeInfo())){
				return node;
			}	
			
		}
				
		return null;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFileHash(String hash) {
		fileHash = hash;
		System.out.println("hash set to "+hash);
	}

	public void readFileHashAsMaster() {
		try {
			setFileHash(versionManager.getGameJarHash());
		} catch (Exception e) {
			System.err.println("I am a master node but I dont have the master file!");
			//e.printStackTrace();
		}
	}

	public String getFileHash() {
		return fileHash;
	}
	
	public ChunkManager getChunkManager(){
		return chunkManager;
	}

	public void setTotalChunkCount(int count) {
		getChunkManager().setTotalChunkCount(count);
	}

}

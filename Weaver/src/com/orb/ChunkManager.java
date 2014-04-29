package com.orb;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

public class ChunkManager {

	WeaverOrb weaverOrb;
	public ChunkManager(WeaverOrb weaverOrb){
		this.weaverOrb=weaverOrb;		
	}
	
	Chunk chunks[];
	static final int CHUNK_LENGTH = 65000;
	
	int totalChunkCount = -1;
	
	public void generateChunksFromFile(){//for seeder

		File myfile = new File(  weaverOrb.getFilePath() );
		byte[] chunkBytes = null;
		
		try {
			chunkBytes = FileUtils.readFileToByteArray(myfile);
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}
		
		System.out.println(" length " + chunkBytes.length);	
		
		
		totalChunkCount = 0;
		
		chunks = new Chunk[(chunkBytes.length / CHUNK_LENGTH) + 1];
		
		for(int i=0;i < chunkBytes.length ;i+=CHUNK_LENGTH){
			int start = i;
			int end = i+ CHUNK_LENGTH;
			
			if(end > chunkBytes.length){
				end = chunkBytes.length ;
			}
								
			chunks[totalChunkCount] = new Chunk(totalChunkCount,Arrays.copyOfRange(chunkBytes, start, end ));
			totalChunkCount++;
		}
		
		System.out.println("chunk count: "+ totalChunkCount);
		
	}
	
	//for leecher, from seeder
	public void setTotalChunkCount(int count){
		totalChunkCount = count;
		
		if(chunks == null){//dont delete chunks
		chunks = new Chunk[count];
		}
	}
	
	
	int nextChunkNeededCounter = 0; // for leecher
	public int getNextLeechChunkId(){

		int answer = -1;
	for(int i=0;i<chunks.length;i++){
		if(chunks[i] == null){
			answer = i;
			break;
		}
	}
		
		return answer;
	}

	
	public void receiveChunk(Chunk newchunk) {
		chunks[newchunk.id] = (Chunk) newchunk.clone();
		
		System.out.println("Got file chunk " + newchunk.id);
		
	}
	
	
	public Chunk getChunkFromId(int id) {
		// TODO Auto-generated method stub
		return chunks[id];
	}
	
	float chunkProgress = 0f;
	public void updateLeeching(){
		int currentCompletedChunks = countCompletedChunks();
		
		if(currentCompletedChunks < totalChunkCount){
			chunkProgress = currentCompletedChunks / (float)totalChunkCount;
			System.out.println("Still need more chunks. Only have " + currentCompletedChunks );
		}else{
			chunkProgress = 1f;
			
			try {
				writeFileFromChunk();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			weaverOrb.shouldCheckVersion = true;
		}
		
		
		
		
	}
	
	private int countCompletedChunks() {
		int count = 0;
		for(Chunk chunk : chunks){
			if(chunk!=null ){				
				count++;
			}			
		}
		
		return count;
	}

	
	public void writeFileFromChunk() throws Exception{//done by leecher, right before they become a seeder
		File myfile = new File(  weaverOrb.getFilePath() );
		
		byte[] allbytes;
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		
		for(Chunk chunk : chunks){
		outputStream.write( chunk.getData() );
		}		
		allbytes = outputStream.toByteArray( );
		
		
		FileUtils.writeByteArrayToFile(myfile, allbytes);
		
		outputStream.close();
	}

	
	public float getChunkProgress() {
		return chunkProgress;
	}

	public int getTotalChunkCount() {
		
		return totalChunkCount;
	}

	

}

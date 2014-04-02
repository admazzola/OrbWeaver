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
								
			chunks[totalChunkCount] = new Chunk(i,Arrays.copyOfRange(chunkBytes, start, end ));
			totalChunkCount++;
		}
		
		System.out.println("chunk count: "+ totalChunkCount);
		
	}
	
	//for leecher, from seeder
	public void setTotalChunkCount(int count){
		count = totalChunkCount;
	}
	
	
	int nextChunkNeededCounter = 0; // for leecher
	public int getNextLeechChunkId(){

		if(nextChunkNeededCounter > totalChunkCount){
			nextChunkNeededCounter = 0; // cycle around
		}
		
		nextChunkNeededCounter++;
		
		return nextChunkNeededCounter - 1;
	}

	public void receiveChunk(Chunk newchunk) {
		chunks[newchunk.id] = (Chunk) newchunk.clone();
	}
	
	
	public Chunk getChunkFromId(int id) {
		// TODO Auto-generated method stub
		return chunks[id];
	}
	
	
	//STILL NEED TO IMPLEMENT THIS
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

	

}

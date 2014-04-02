package com.orb;

import com.network.NodeInfo;

public class QueuedChunkRequest {

	public int chunkId;
	public NodeInfo senderInfo;
	
	public QueuedChunkRequest(int id, NodeInfo info) {
		chunkId = id;
		senderInfo = info;
	}

	
}

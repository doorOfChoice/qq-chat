package cn.seeonce.runner;

import cn.seeonce.core.EntranceChatServer;
import cn.seeonce.core.EntranceLoginServer;

public class ServerRunner {

	public static void main(String[] args) {
		EntranceLoginServer.newServer();
		EntranceChatServer.newServer();
	}

}

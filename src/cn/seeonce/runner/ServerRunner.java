package cn.seeonce.runner;

import cn.seeonce.entrance.EntranceChatServer;
import cn.seeonce.entrance.EntranceLoginServer;

public class ServerRunner {

	public static void main(String[] args) {
		EntranceLoginServer.newServer();
		EntranceChatServer.newServer();
	}

}

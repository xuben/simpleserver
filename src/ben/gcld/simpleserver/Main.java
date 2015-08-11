package ben.gcld.simpleserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
	
	public static void main(String[] args) {
		// 初始服务器配置
		ServerConfig.loadConfig();
		// 初始化命令配置
		CommandConfig.loadConfig();
		
		ServerSocket server;
		try {
			server = new ServerSocket(ServerConfig.PORT);
			while (true) {
				// 建立新的连接
				Socket socket = server.accept();
				// 创建处理线程
				Thread thread = new Thread(new SimpleServer(socket));
				thread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

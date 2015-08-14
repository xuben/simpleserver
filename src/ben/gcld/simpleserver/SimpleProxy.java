package ben.gcld.simpleserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 向目标服务器发送并接受请求
 * @author xuben
 *
 */
public class SimpleProxy implements Runnable {

	// tcp连接实例
	private Socket socket;
	// 需要读取的数据长度
	private int readLength;
	// 需要发送给代理服务器的数据包队列
	private LinkedBlockingQueue<Object> sendQueue;
	// 从代理服务器接收到的数据包队列
	private LinkedBlockingQueue<Object> receiveQueue;
	
	public SimpleProxy(LinkedBlockingQueue<Object> sendQueue, 
			LinkedBlockingQueue<Object> receiveQueue) {
		this.sendQueue = sendQueue;
		this.receiveQueue = receiveQueue;
	}
	
	@Override
	public void run() {
		try {
			// 与服务器建立连接
			socket = new Socket(ServerConfig.PROXY_IP, ServerConfig.PROXY_PORT);
			System.out.printf("[SimpleProxy] connected to %s:%d\n", 
					ServerConfig.PROXY_IP, ServerConfig.PROXY_PORT);
			while (!Thread.interrupted()) {
				// 是否有需要发送给服务器的数据
				if (!sendQueue.isEmpty()) {
					byte[] sendBytes = (byte[]) sendQueue.poll();
					if (null != sendBytes) {
						OutputStream out = socket.getOutputStream();
						out.write(sendBytes);
					}
				}
				
				// 是否从服务器接收到数据
				InputStream in = socket.getInputStream();
				// 需要读取的数据长度未设置
				if (readLength == 0) {
					// 设置需要读取的数据长度
					if (in.available() >= 4) {
						readLength = IOUtil.readInt(in);
					} else {
						Thread.sleep(100);
						continue;
					}
				}
				// 需要的数据是否足够
				if (in.available() < readLength) {
					Thread.sleep(100);
					continue;
				}
				
				byte[] receiveBytes = new byte[4+readLength];
				// 写长度
				IOUtil.writeInt(readLength, receiveBytes, 0);
				// 写内容
				in.read(receiveBytes, 4, readLength);
				// 加入接收队列
				receiveQueue.offer(receiveBytes);
				
				// 重置已读取到的包长度
				readLength = 0;
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			System.out.println("[SimpleProxy] disconnected");
		}
	}
}

package ben.gcld.simpleserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.DeflaterOutputStream;

/**
 * 根据请求的命令从配置文件中读取响应内容并返回
 * 
 * @author xuben
 *
 */
public class SimpleServer implements Runnable {

	// tcp连接实例
	private Socket socket;
	// 需要读取的数据长度
	private int readLength;
	// 上次收到请求的时间
	private long lastRequestTime;
	// 代理线程
	private Thread proxy;
	// 需要发送给代理服务器的数据包队列
	private LinkedBlockingQueue<Object> sendQueue;
	// 从代理服务器接收到的数据包队列
	private LinkedBlockingQueue<Object> receiveQueue;
	
	public SimpleServer(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			System.out.println("[SimpleServer] connected");
			lastRequestTime = System.currentTimeMillis();
			// 处理flash策略文件请求
			int result = 0;
			while(true) {
				// 判断连接是否超时
				if (System.currentTimeMillis() - lastRequestTime 
						>= ServerConfig.CONNECTION_TIMEOUT) {
					System.out.println("[SimpleServer] connection timeout");
					return;
				}
				// 处理请求
				result = checkFlashPolicy();
				if (result == 0) { // flash策略文件请求
					System.out.println("[SimpleServer] policy file request");
					return;
				} else if (result > 0) { // 不是flash策略文件请求
					// 初始化代理
					initProxy();
					readLength = result;
					break;
				} else {
					Thread.sleep(100);
					continue;
				}
			}
			
			// 处理游戏内请求
			while (proxy == null || proxy.isAlive()) {
				// 判断连接是否超时
				if (System.currentTimeMillis() - lastRequestTime 
						>= ServerConfig.CONNECTION_TIMEOUT) {
					System.out.println("[SimpleServer] connection timeout");
					return;
				}
				
				// 是否从服务器接收到数据
				if (null != receiveQueue && !receiveQueue.isEmpty()) {
					byte[] receiveBytes = (byte[]) receiveQueue.poll();
					if (null != receiveBytes) {
						OutputStream out = socket.getOutputStream();
						out.write(receiveBytes);
					}
				}
				
				// 是否从客户端接收到数据
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

				lastRequestTime = System.currentTimeMillis();
				
				// 读取需要的数据
				// 读取命令
				String command32 = IOUtil.readString(in, 32);
				String command = command32.trim();
				// 读取请求号
//				int requestId = readInt(in);
				byte[] requestIdBytes = new byte[4];
				in.read(requestIdBytes);
				// 读取内容
//				String content = readString(in, readLength - 36);
				// 消耗掉内容
				byte[] contentBytes = new byte[readLength - 36];
				in.read(contentBytes);

				// 验证命令
				if (null != command) {
					// 获取命令对应的数据
					String data = CommandConfig.getData(command);
					if (null != data) {
						byte[] dataBytes = data.getBytes("utf8");
						// 获取输出流
						OutputStream out = socket.getOutputStream();
						// 需要压缩数据
						if (ServerConfig.DATA_COMPRESSION) {
							ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
							DeflaterOutputStream zlibOut = new DeflaterOutputStream(
									byteArrayOut);
							zlibOut.write(dataBytes);
							// 如果不flush可能不会返回完整的数据
							zlibOut.flush();
							zlibOut.close();
							dataBytes = byteArrayOut.toByteArray();
						}
						// 计算输出的长度
						int writeLength = 36 + dataBytes.length;
						// 输出数据
						out.write(IOUtil.writeInt(writeLength));
						// out.write(Arrays.copyOf(command.getBytes(), 32));
						out.write(command32.getBytes());
						out.write(requestIdBytes);
						out.write(dataBytes);
						out.flush();
					} else if (null != sendQueue) { // 代理模式
						// 发送给目标服务器
						byte[] sendBytes = new byte[4+readLength];
						IOUtil.writeInt(readLength, sendBytes, 0);
						System.arraycopy(command32.getBytes(), 0, sendBytes, 4, 32);
						System.arraycopy(requestIdBytes, 0, sendBytes, 36, 4);
						System.arraycopy(contentBytes, 0, sendBytes, 40, contentBytes.length);
						sendQueue.offer(sendBytes);
					}
				}
				
				// 重置已读取到的包长度
				readLength = 0;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			// 终止代理线程
			if (null != proxy) {
				proxy.interrupt();
			}
			System.out.println("[SimpleServer] disconnected");
		}
	}
	
	/**
	 * 处理flash策略文件请求
	 * @return 未收到请求返回-1 处理策略文件返回0 否则返回包长度
	 * @throws IOException
	 */
	private int checkFlashPolicy() throws IOException {
		InputStream in = socket.getInputStream();
		if (in.available() >= 4) {
			byte[] content = new byte[4];
			in.read(content);
			// 判断是否为策略文件请求
			if (new String(content).equalsIgnoreCase("<pol")) {
				// 读取所有内容
				while (in.available() > 0) {
					in.read();
				}
				// 返回响应
				OutputStream out = socket.getOutputStream();
				out.write(ServerConfig.FLASH_POLICY_RESPONSE);
				return 0;
			}
			// 不是策略文件请求
			return IOUtil.readInt(content);
		}
		return -1;
	}
	
	/**
	 * 初始化代理
	 */
	private void initProxy() {
		// 代理模式
		if (!ServerConfig.STANDALONE_MODE) {
			sendQueue = new LinkedBlockingQueue<Object>();
			receiveQueue = new LinkedBlockingQueue<Object>();
			// 启动代理线程
			proxy = new Thread(new SimpleProxy(sendQueue, receiveQueue));
			proxy.start();
		}
	}
}

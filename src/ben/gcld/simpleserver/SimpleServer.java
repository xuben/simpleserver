package ben.gcld.simpleserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

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

	public SimpleServer(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			System.out.println("[SimpleServer]: connected");
			lastRequestTime = System.currentTimeMillis();
			while (true) {
				// 判断连接是否超时
				if (System.currentTimeMillis() - lastRequestTime 
						>= ServerConfig.CONNECTION_TIMEOUT) {
					System.out.println("[SimpleServer]: connection timeout");
					return;
				}
				InputStream in = socket.getInputStream();
				// 需要读取的数据长度未设置
				if (readLength == 0) {
					// 设置需要读取的数据长度
					if (in.available() >= 4) {
						readLength = readInt(in);
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
				String command = readString(in, 32).trim();
				// 读取请求号
				int requestId = readInt(in);
				// 读取内容
				String content = readString(in, readLength - 36);

				// 重置已读取到的包长度
				readLength = 0;

				// 输出
				System.out.println("command:" + command);
				System.out.println("requestId:" + requestId);
				System.out.println("content:" + content);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			System.out.println("[SimpleServer]: disconnected");
		}
	}

	/**
	 * read an integer value from the input stream
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private int readInt(InputStream in) throws IOException {
		byte[] byteArray = new byte[4];
		in.read(byteArray);
		return byteArray[0] << 24 + byteArray[1] << 16 + byteArray[2] << 8 + byteArray[3];
	}

	/**
	 * read a given length string from the input stream
	 * 
	 * @param in
	 * @param length
	 * @return
	 * @throws IOException
	 */
	private String readString(InputStream in, int length) throws IOException {
		byte[] byteArray = new byte[length];
		in.read(byteArray);
		return new String(byteArray);
	}
}

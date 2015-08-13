package ben.gcld.simpleserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
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
				String command32 = readString(in, 32);
				String command = command32.trim();
				// 读取请求号
//				int requestId = readInt(in);
				byte[] requestIdBytes = new byte[4];
				in.read(requestIdBytes);
				// 读取内容
//				String content = readString(in, readLength - 36);
				// 消耗掉内容
				readString(in, readLength - 36);

				// 重置已读取到的包长度
				readLength = 0;
				
				// 验证命令
				if (command == null) {
					continue;
				}
				
				// 获取命令对应的数据
				String data = CommandConfig.getData(command);
				byte[] dataBytes = data.getBytes("utf8");
				// 获取输出流
				OutputStream out = socket.getOutputStream();
				// 需要压缩数据
				if (ServerConfig.DATA_COMPRESSION) {
					ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
					DeflaterOutputStream zlibOut = new DeflaterOutputStream(byteArrayOut);
					zlibOut.write(dataBytes);
					// 如果不flush可能不会返回完整的数据
					zlibOut.flush();
					zlibOut.close();
					dataBytes = byteArrayOut.toByteArray();
				}
				// 计算输出的长度
				int writeLength = 36 + dataBytes.length;
				// 输出数据
				out.write(convertInt(writeLength));
//				out.write(Arrays.copyOf(command.getBytes(), 32));
				out.write(command32.getBytes());
				out.write(requestIdBytes);
				out.write(dataBytes);
				out.flush();
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
		return (byteArray[0]&0xff) << 24 
				| (byteArray[1]&0xff) << 16 
				| (byteArray[2]&0xff) << 8 
				| (byteArray[3]&0xff) << 0;
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
	
	/**
	 * convert integer value to byte array
	 * @param value
	 * @return
	 */
	private byte[] convertInt(int value) {
		byte[] byteArray = new byte[4];
		byteArray[0] = (byte) (value >>> 24);
		byteArray[1] = (byte) (value >>> 16);
		byteArray[2] = (byte) (value >>> 8);
		byteArray[3] = (byte) (value >>> 0);
		return byteArray;
	}
}

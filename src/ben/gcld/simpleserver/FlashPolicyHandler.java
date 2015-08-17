package ben.gcld.simpleserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * flash策略文件处理
 * @author xuben
 *
 */
public class FlashPolicyHandler {
	
	/**
	 * 处理请求
	 * @param socket
	 * @return 未收到请求返回-1 处理策略文件返回0 否则返回包长度
	 * @throws IOException
	 */
	public int handle(Socket socket) throws IOException {
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
			// 不是策略文件请求，需要把已读取的字节放回去
			return IOUtil.readInt(content);
		}
		return -1;
	}
}

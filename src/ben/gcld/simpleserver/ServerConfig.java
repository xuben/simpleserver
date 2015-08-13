package ben.gcld.simpleserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 服务器配置
 * @author xuben
 *
 */
public class ServerConfig {
	
	/**服务器端口号*/
	public static int PORT = 5577;
	/**连接超时时间*/
	public static int CONNECTION_TIMEOUT = 3*60*1000;
	/**是否压缩发送的数据*/
	public static boolean DATA_COMPRESSION = true;
	/**命令配置文件路径*/
	public static String COMMAND_CONFIG_PATH = File.separator 
			+ "conf" + File.separator + "commands";
	/**是否打印出详细的请求响应数据*/
	public static boolean PRINT_VERBOSE_DATA = false;
	/**是否打印出成功的请求响应数据*/
	public static boolean PRINT_SUCCESS_DATA = true;
	/**是否打印出未配置的请求响应数据*/
	public static boolean PRINT_FAILURE_DATA = true;
	
	/**配置文件路径*/
	public static String SERVER_CONFIG_PATH = File.separator 
			+ "conf" + File.separator + "server.properties";
	
	/**
	 * 读取配置文件
	 */
	public static void loadConfig() {
		System.out.println("[ServerConfig]: loading server config");
		Properties prop = new Properties();
		File f = new File(System.getProperty("user.dir") + SERVER_CONFIG_PATH);
		try {
			FileInputStream in = new FileInputStream(f);
			prop.load(in);
			
			PORT = Integer.parseInt(
					prop.getProperty("server.port", "5577"));
			CONNECTION_TIMEOUT = Integer.parseInt(
					prop.getProperty("server.connection.timeout", 3*60*1000+""));
			DATA_COMPRESSION = Boolean.parseBoolean(
					prop.getProperty("server.data.compression", "true"));
			if (prop.containsKey("server.command.path")) {
				COMMAND_CONFIG_PATH = prop.getProperty("server.command.path");
			}
			PRINT_VERBOSE_DATA = Boolean.parseBoolean(
					prop.getProperty("server.data.print.verbose"));
			PRINT_SUCCESS_DATA = Boolean.parseBoolean(
					prop.getProperty("server.data.print.success"));
			PRINT_FAILURE_DATA = Boolean.parseBoolean(
					prop.getProperty("server.data.print.failure"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("[ServerConfig]: server config loaded");
	}
}

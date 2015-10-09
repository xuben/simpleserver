package ben.gcld.simpleserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

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
	/**不打印请求响应数据的命令*/
	public static ConcurrentHashMap<String, String> printIgnoreMap = new ConcurrentHashMap<String, String>();
	/**服务器运行模式*/
	public static boolean STANDALONE_MODE = true;
	/**proxy模式下目标服务器地址*/
	public static String PROXY_IP = "10.5.201.45";
	/**proxy模式下目标服务器端口号*/
	public static int PROXY_PORT = 8001;
	
	/**flash策略文件响应*/
	public static byte[] FLASH_POLICY_RESPONSE = 
			"<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"*\" /></cross-domain-policy>\0".getBytes();
	/**角色信息接口*/
	public static final String COMMAND_ROLE_INFO = "player@getPlayerInfo";
	
	/**配置文件路径*/
	public static String SERVER_CONFIG_PATH = File.separator 
			+ "conf" + File.separator + "server.properties";
	/**配置文件*/
	private static File f;
	/**配置文件上次修改时间*/
	private static AtomicLong modifiedTime = new AtomicLong();
	/**是否初始化过*/
	private static boolean init;
	
	/**
	 * 读取配置文件
	 */
	public static void loadConfig() {
		// 已初始化过并且文件未修改
		if (null != f && f.lastModified() <= modifiedTime.get()) {
			return;
		}
		if (null == f) { // 未初始化过
			System.out.println("[ServerConfig] loading server config");
			f = new File(System.getProperty("user.dir") + SERVER_CONFIG_PATH);
		} else { // 文件有修改
			System.out.println("[ServerConfig] reloading server config");
		}
		try {
			loadConfig1(f);
			init = true;
			System.out.println("[ServerConfig] server config loaded");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 读取配置文件
	 * @param f
	 * @throws IOException
	 */
	private static void loadConfig1(File f) throws IOException {
		modifiedTime.set(f.lastModified());
		
		FileInputStream in = new FileInputStream(f);
		Properties prop = new Properties();
		prop.load(in);
		
		PORT = Integer.parseInt(
				prop.getProperty("server.port", PORT + ""));
		CONNECTION_TIMEOUT = Integer.parseInt(
				prop.getProperty("server.connection.timeout", CONNECTION_TIMEOUT + ""));
		DATA_COMPRESSION = Boolean.parseBoolean(
				prop.getProperty("server.data.compression", DATA_COMPRESSION + ""));
		COMMAND_CONFIG_PATH = prop.getProperty("server.command.path", SERVER_CONFIG_PATH);
		PRINT_VERBOSE_DATA = Boolean.parseBoolean(
				prop.getProperty("server.data.print.verbose", PRINT_VERBOSE_DATA + ""));
		PRINT_SUCCESS_DATA = Boolean.parseBoolean(
				prop.getProperty("server.data.print.success", PRINT_SUCCESS_DATA + ""));
		PRINT_FAILURE_DATA = Boolean.parseBoolean(
				prop.getProperty("server.data.print.failure", PRINT_FAILURE_DATA + ""));
		
		printIgnoreMap.clear();
		String printIgnores = prop.getProperty("server.data.print.ignore", null);
		if (null != printIgnores && printIgnores.length() > 0) {
			String[] splits = printIgnores.split(",");
			for (String printIgnore : splits) {
				printIgnoreMap.put(printIgnore, printIgnore);
			}
		}
		
		// 该参数重启生效
		if (!init) {
			STANDALONE_MODE = Boolean.parseBoolean(prop.getProperty(
					"server.mode.standalone", STANDALONE_MODE + ""));
		}
		PROXY_IP = prop.getProperty("server.mode.proxy.ip", PROXY_IP);
		PROXY_PORT = Integer.parseInt(
				prop.getProperty("server.mode.proxy.port", PROXY_PORT + ""));
	}
}

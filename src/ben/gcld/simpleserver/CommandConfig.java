package ben.gcld.simpleserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

/**
 * 命令配置
 * @author xuben
 *
 */
public class CommandConfig {

	/**命令数据*/
	private static ConcurrentHashMap<String, String> commandMap = new ConcurrentHashMap<String, String>();
	/**文件最后修改时间*/
	private static ConcurrentHashMap<String, Long> modifiedMap = new ConcurrentHashMap<String, Long>();
	/**错误数据*/
	private static String commandError = "{\"msg\":\"命令未配置\"}";
	/**配置文件根目录*/
	private static File rootFile;
	
	/**
	 * 读取配置文件
	 */
	public static void loadConfig() {
		// 配置文件已加载过
		if (null != rootFile) {
			return;
		}
		rootFile = new File(System.getProperty("user.dir") + ServerConfig.COMMAND_CONFIG_PATH);
		System.out.println("[CommandConfig]: loading command configs " + rootFile.getAbsolutePath());
		try {
			loadFiles(rootFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("[CommandConfig]: command configs loaded");
	}
	
	/**
	 * 检测配置文件是否需要重新加载
	 * 如果需要则进行加载
	 */
	public static void checkAndLoadConfig() {
		// 配置文件未加载过
		if (null == rootFile) {
			return;
		}
		try {
			loadFiles(rootFile);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 读取文件或目录下所有文件的配置
	 * @param f
	 * @throws IOException
	 */
	private static void loadFiles(File f) throws IOException {
		// 验证文件有效性
		if (f == null) {
			return;
		}
		if (f.isDirectory()) { // 处理目录
			for (File file : f.listFiles()) {
				loadFiles(file);
			}
		} else { // 处理文件
			if (f.getName().toLowerCase().endsWith(".json")) {
				checkAndLoadFile(f);
			}
		}
	}
	
	/**
	 * 检测并读取文件
	 * @param f
	 * @throws FileNotFoundException 
	 */
	private static void checkAndLoadFile(File f) throws FileNotFoundException {
		Long time = modifiedMap.get(f.getAbsolutePath());
		// 是否需要重新加载
		if (null != time && time >= f.lastModified()) {
			return;
		}
		if (null == time) { // 加载新文件
			System.out.println("[CommandConfig]: loading command config " + f.getName());
		} else if (time < f.lastModified()) { // 文件有修改，重新加载
			System.out.println("[CommandConfig]: reloading command config " + f.getName());
		}
		loadFile(f);
		System.out.println("[CommandConfig]: command config " + f.getName() + " loaded");
	}
	
	/**
	 * 读取文件配置
	 * @param f
	 * @throws FileNotFoundException 
	 */
	private static void loadFile(File f) throws FileNotFoundException {
		Scanner scanner = new Scanner(f);
		StringBuilder builder = new StringBuilder();
		// 从文件中读取json字符串
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			// 去掉注释行，注释用4斜杠，避免与http://冲突
			int commentIndex = line.indexOf("////");
			if (commentIndex >= 0) {
				line = line.substring(0, commentIndex);
			}
			builder.append(line);
		}
		// 关闭输入流
		scanner.close();
		
		// 更新文件修改时间
		modifiedMap.put(f.getAbsolutePath(), f.lastModified());
		
		// 解析命令
		if (builder.length() > 0) {
			List<JSONObject> commands = JSON.parseArray(
					builder.toString(), JSONObject.class);
			for (JSONObject command : commands) {
				String cmdName = command.getString("command");
				String cmdData = command.getString("data");
				commandMap.put(cmdName, cmdData);
			}
		}
	}
	
	/**
	 * 获取命令对应的数据
	 * @param command
	 * @return
	 */
	public static String getData(String command) {
		// 验证参数有效性
		if (command == null) {
			return null;
		}
		String data = commandMap.get(command);
		int state = 1;
		// 命令未配置
		if (data == null) {
			data = commandError;
			state = 0;
		}
		// 输出错误数据
		if (state == 0) {
			if (ServerConfig.PRINT_FAILURE_DATA) {
				System.out.printf("command:%s, not configured\n", command);
			}
		}
		// 输出成功数据
		else if (ServerConfig.PRINT_SUCCESS_DATA) {
			// 输出详细数据
			if (ServerConfig.PRINT_VERBOSE_DATA) {
				System.out.printf("command:%s, data:%s\n", command, data);
			} else {
				System.out.printf("command:%s, success\n", command);
			}
		}
		return String.format("{\"action\":{\"state\":%d,\"data\":%s}}", state, data);
	}
}
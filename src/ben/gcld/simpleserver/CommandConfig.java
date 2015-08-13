package ben.gcld.simpleserver;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 命令配置
 * @author xuben
 *
 */
public class CommandConfig {

	/**命令数据*/
	private static HashMap<String, String> commandMap = new HashMap<String, String>();
	/**错误数据*/
	private static String commandError = "{\"msg\":\"命令未配置\"}";
	
	/**配置文件路径*/
	public static String COMMAND_CONFIG_PATH = File.separator 
			+ "conf" + File.separator + "commands";
	
	/**
	 * 读取配置文件
	 */
	public static void loadConfig() {
		System.out.println("[CommandConfig]: start loading command configs");
		File f = new File(System.getProperty("user.dir") + COMMAND_CONFIG_PATH);
		try {
			loadFile(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("[CommandConfig]: command configs loaded");
	}
	
	/**
	 * 读取文件或目录下所有文件的配置
	 * @param f
	 * @throws IOException
	 */
	private static void loadFile(File f) throws IOException {
		// 验证文件有效性
		if (f == null) {
			return;
		}
		// 处理目录
		if (f.isDirectory()) {
			for (File file : f.listFiles()) {
				loadFile(file);
			}
		} else {
			// 处理文件
			System.out.println("[CommandConfig]: start loading command config " + f.getName());
			Scanner scanner = new Scanner(f);
			StringBuilder builder = new StringBuilder();
			// 从文件中读取json字符串
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				// 去掉注释行
				int commentIndex = line.indexOf("//");
				if (commentIndex >= 0) {
					line = line.substring(0, commentIndex);
				}
				builder.append(line);
			}
			// 关闭输入流
			scanner.close();
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
			System.out.println("[CommandConfig]: command config " + f.getName() + " loaded");
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
		System.out.printf("command:%s, data:%s\n", command, data);
		return String.format("{\"action\":{\"state\":%d,\"data\":%s}}", state, data);
	}
}
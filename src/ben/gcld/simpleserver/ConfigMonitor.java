package ben.gcld.simpleserver;


/**
 * 监控配置文件的更改并重新加载
 * 
 * @author xuben
 *
 */
public class ConfigMonitor implements Runnable {

	@Override
	public void run() {
		System.out.println("[ConfigMonitor] command monitor started");
		while (true) {
			try {
				ServerConfig.loadConfig();
				CommandConfig.checkAndLoadConfig();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

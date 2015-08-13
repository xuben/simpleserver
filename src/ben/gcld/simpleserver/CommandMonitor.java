package ben.gcld.simpleserver;


/**
 * 监控命令文件的更改并重新加载
 * 
 * @author xuben
 *
 */
public class CommandMonitor implements Runnable {

	@Override
	public void run() {
		System.out.println("[CommandMonitor]: command monitor started");
		while (true) {
			try {
				CommandConfig.checkAndLoadConfig();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

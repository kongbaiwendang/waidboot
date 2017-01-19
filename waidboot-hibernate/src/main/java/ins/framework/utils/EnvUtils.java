package ins.framework.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 获取环境相关信息的工具类
 */
public class EnvUtils {
	private static String SERVER_IP = null;
    private static final Logger logger = LoggerFactory.getLogger(EnvUtils.class);
	static {
		try {
			// 缓存服务器IP
			SERVER_IP = getInnerServerIp();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			SERVER_IP = "127.0.0.1";
		}
	}

	/**
	 * 获取请求的客户端的IP<br>
	 * 说明：如果使用了负载均衡器，则调用request.getRemoteHost()方法返回的是负载均衡器的IP， 而不是客户端的实际IP
	 * ，一般的负载均衡在发送请求到一台服务器时，会将客户端的真实IP放到header信息中，所以可以通过这种方式获取客户端的真实IP)
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @return 请求的客户端的IP
	 */
	public static String getClientIp(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * 得到Web应用的classes目录
	 * 
	 * @return Web应用的classes目录
	 */
	public static String getWebClassesPath() {
		String path = new File(Thread.currentThread().getContextClassLoader()
				.getResource("").getPath()).getPath();
		path = path.replace("%20", " ");
		return path;
	}

	/**
	 * 得到应用所在服务器的IP
	 * 
	 * @return 应用所在服务器的IP
	 */
	public static String getServerIp() {
		return SERVER_IP;
	}

	public static void main(String[] args) {
		System.out.println("IP=[" + getServerIp() + "]");
	}

	private static String getInnerServerIp() {
		String osName = System.getProperty("os.name").toLowerCase();
		String ip = null;
		try {
			if (osName.startsWith("linux")) {
				ip = getLocalIp("/sbin/ip addr", "inet ", "", "/");
			} else if (osName.startsWith("window")) {
				ip = getLocalIp("ipconfig /all", "IP Address", ": ", "(");
				if (ip == null) {
					ip = getLocalIp("ipconfig /all", "IPv4 地址", ":", "(");
				}
			} else if (osName.startsWith("aix")) {
				ip = getLocalIp("ifconfig -a", "inet ", "", "netmask");
			} else {
				throw new IllegalStateException("Not support OS:" + osName);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new IllegalStateException(e.toString(), e);
		}
		if (ip == null) {
			ip = "127.0.0.1";
		}
		return ip;
	}

	/**
	 * 返回本地IP
	 * 
	 * @param cmd
	 *            执行命令
	 * @param indLine
	 *            IP行标记
	 * @param indStart
	 *            IP开始位置标记
	 * @param indStart
	 *            IP结束位置标记
	 * @return 本地IP
	 * @throws Exception
	 */
	private static String getLocalIp(String cmd, String indLine,
			String indStart, String indEnd) throws Exception {
		Runtime rt = Runtime.getRuntime();
		Process process = rt.exec(cmd);

		BufferedReader in = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
		try{	
			String s = in.readLine();
			while (null != s) {
				s = s.trim();
				if (s.startsWith(indLine)) {
					s = s.substring(indLine.length()).trim();
					int pos = s.indexOf(indStart);
					String ip = s.substring(indStart.length() + pos).trim();
					pos = ip.indexOf(indEnd);
					if (pos > -1) {
						ip = ip.substring(0, pos);
					}
					if (!ip.startsWith("127.")) {
						return ip;
					}
				}
			}
			in.close();
		}catch(IOException ioe) {
			logger.error(ioe.getMessage(), ioe);
		}finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.debug(e.getMessage(), e);
				}
				in = null;
			}
		}	
		return null;
	}
}
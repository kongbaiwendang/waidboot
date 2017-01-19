package ins.framework.common;

import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * 环境配置
 * 
 * <pre>
 * 与系统环境相关的配置参数
 * </pre>
 * 
 * @author amosryan
 * @since 2010-07-30
 * @version V1.0
 * 
 */
public class SysConfig {

	private static Logger logger = Logger.getLogger(SysConfig.class);

	private static Properties prop = new Properties();

	static {
		init();
	}

	private static void init() {
		String envConfigFile = "/SystemConfig.properties";
		try {
			prop.load(SysConfig.class.getResourceAsStream(envConfigFile));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("请检查系统参数配置文件：" + envConfigFile);
		}
		
	}

	public static String get(String key) {
		return get(key , null);
	}

	public static String get(String key , String defaultValue) {
		String value = prop.getProperty(key , defaultValue);
		logger.debug("SysConfig:"+key+":"+value);
		return value;
	}
}

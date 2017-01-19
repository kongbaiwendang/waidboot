package ins.framework.common;

import javax.servlet.ServletContext;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Service工厂
 * @author lxp
 */
public class ServiceFactory {// NOPMD
	private static WebApplicationContext context;

	private ServiceFactory() {
	}

	public static void initServiceFactory(ServletContext servletContext) {
		context = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
	}

	/**
	 * 获取Service
	 * 
	 * <pre>
	 * CodeService codeService = (CodeService) ServiceFactory.getService(pageContext, &quot;codeService&quot;);
	 * </pre>
	 * @param servletContext
	 *            页面上下文
	 * @param serviceName
	 *            service名称
	 * @return Service对象
	 */
	public static Object getService(ServletContext servletContext, String serviceName) {
		return context.getBean(serviceName);
	}

	/**
	 * 获取Service
	 * 
	 * <pre>
	 * CodeService codeService = (CodeService) ServiceFactory.getService(&quot;codeService&quot;);
	 * </pre>
	 * @param serviceName
	 *            service名称
	 * @return Service对象
	 */
	public static Object getService(String serviceName) {
		return context.getBean(serviceName);
	}
}

package ins.framework.utils;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class RequestUtils {
	/**
	 * 将request转化为Map<String, String>
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> convertToMap(
			HttpServletRequest request) {
		HashMap<String, String> paramMap = new HashMap<String, String>();
		Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			paramMap.put(name, request.getParameter(name));
		}

		return paramMap;
	}

	/**
	 * 获取int型参数值
	 * 
	 * @param paramMap
	 *            request.getParameterMap()得到的Map
	 * @param paramName
	 *            参数名
	 * @param defaultValue
	 *            默认值（无内容时用默认值）
	 * @return 参数值
	 */
	public static int getInt(Map<String, String[]> paramMap, String paramName,
			int defaultValue) {
		String[] paramValue = paramMap.get(paramName);
		int value = defaultValue;
		try {
			if (paramValue != null && paramValue.length > 0) {
				value = Integer.valueOf(paramValue[0]);
			}
		} catch (Exception e) {
			value = defaultValue;
		}
		return value;
	}

	/**
	 * 获取String型参数值
	 * 
	 * @param paramMap
	 *            request.getParameterMap()得到的Map
	 * @param paramName
	 *            参数名
	 * @param defaultValue
	 *            默认值（无内容时用默认值）
	 * @return 参数值
	 */
	public static String getString(Map<String, String[]> paramMap,
			String paramName, String defaultValue) {
		String[] paramValue = paramMap.get(paramName);
		String value = defaultValue;
		try {
			if (paramValue != null && paramValue.length > 0) {
				value = paramValue[0];
			}
		} catch (Exception e) {
			value = defaultValue;
		}
		return value;
	}
}

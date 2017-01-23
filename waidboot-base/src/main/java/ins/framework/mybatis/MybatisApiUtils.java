package ins.framework.mybatis;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * MyBatisApi的工具类
 * 
 * @author zhouxianli
 *
 */
public class MybatisApiUtils {

	/** 默认分页大小 */
	public static final int DEFAULT_PAGE_SIZE = 10;
	/** 最大分页大小 */
	public static final int MAX_PAGE_SIZE = 3000;

	public static PageParam getPageParam() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		return getPageParam(request);
	}

	public static PageParam getPageParam(HttpServletRequest request) {

		int _pageSize = 10;
		int _pageNo = 0;
		int _totalCount = 0;
		_pageNo = getParameterValue(request, "_pageNo", 1);
		if (_pageNo < 1) {
			_pageNo = 1;
		}

		_pageSize = getParameterValue(request, "_pageSize", DEFAULT_PAGE_SIZE);
		if (_pageSize < 0) {
			_pageSize = DEFAULT_PAGE_SIZE;
		}
		if (_pageSize > MAX_PAGE_SIZE) {
			throw new IllegalArgumentException("pageSize exceeded maxPageSize[" + MAX_PAGE_SIZE + "]");
		}
		PageParam pageParam = new PageParam(_pageNo, _pageSize);
		_totalCount = getParameterValue(request, "_totalCount", 0);
		if (_totalCount > 0) {
			pageParam.setContainsTotalCount(false);
		}
		return pageParam;
	}

	public static int getParameterValue(HttpServletRequest request, String name, int def) {
		String str = request.getParameter(name);
		int value = def;
		if (str != null) {
			value = Integer.parseInt(str, 10);
		}
		return value;
	}

	public String getParameterValue(HttpServletRequest request, String name, String def) {
		String str = request.getParameter(name);
		String value = def;
		if (str != null) {
			value = str;

		}
		return value;
	}
}

package ins.framework.dao.database.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库注入检查工具类
 *
 */
public class InjectionCheckUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(InjectionCheckUtils.class);

	/**
	 * 工具类禁止实例化
	 */
	private InjectionCheckUtils() {

	}

	/**
	 * 判断是否合法的SQL
	 * 
	 * @param sql
	 *            sql语句
	 * @return 合法返回true，否则返回false
	 */
	public static boolean isValidSql(String sql) {
		// 白名单验证，sql中只允许
		// 大小写字母，数字，空格，大于小于号《，》，等号=，星号*，感叹号！，下划线_,问号?,冒号:,点。，斜线/和减号-
		String reg = "^[([a-zA-Z])([0-9])(\\s)([><=*!_?:.(),-/])(\\+)]*$";
		boolean valid = sql.matches(reg);
		if (!valid) {
			LOGGER.warn("Invalid sql [{}]", sql);
		}
		return valid;
	}

	/**
	 * 判断是否合法的HQL
	 * 
	 * @param hql
	 *            hql语句
	 * @return 合法返回true，否则返回false
	 */
	public static boolean isValidHql(String hql) {
		// 白名单验证，hql中只允许
		// 大小写字母，数字，空格，大于小于号《，》，等号=，星号*，感叹号！，下划线_,问号?,冒号:,点。，斜线/和减号-
		String reg = "^[([a-zA-Z])([0-9])(\\s)([><=*!_?:.(),-/])(\\+)]*$";
		boolean valid = hql.matches(reg);
		if (!valid) {
			LOGGER.warn("Invalid hql [{}]", hql);
		}
		return valid;
	}

	/**
	 * 判断是否合法的名称(如表名、字段名、sequence名称等)
	 * 
	 * @param name
	 *            名称(如表名、字段名、sequence名称等)
	 * @return 合法返回true，否则返回false
	 */
	public static boolean isValidName(String name) {
		// 白名单验证，name中只允许
		// 大小写字母，数字，下划线_
		String reg = "^[([a-zA-Z])([0-9])([_])]*$";
		return name.matches(reg);
	}

	/**
	 * 检查是否合法的名称(如表名、字段名、sequence名称等)
	 * 
	 * @param name
	 *            名称(如表名、字段名、sequence名称等)
	 * @exception 不合法时抛出IllegalArgumentException
	 */
	public static void checkValidName(String name) {
		if (!isValidName(name)) {
			throw new IllegalArgumentException("Invalid name.");
		}
	}

	/**
	 * 检查是否合法的SQL
	 * 
	 * @param sql
	 *            sql语句
	 * @exception 不合法时抛出IllegalArgumentException
	 */
	public static void checkValidSql(String sql) {
		if (!isValidHql(sql)) {
			throw new IllegalArgumentException("Invalid sql.");
		}
	}

	/**
	 * 判断是否合法的HQL
	 * 
	 * @param hql
	 *            hql语句
	 * @exception 不合法时抛出IllegalArgumentException
	 */
	public static void checkValidHql(String hql) {
		if (!isValidHql(hql)) {
			throw new IllegalArgumentException("Invalid hql.");
		}
	}
}
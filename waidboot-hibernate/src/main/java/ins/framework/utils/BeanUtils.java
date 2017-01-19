package ins.framework.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * 扩展Apache Commons BeanUtils, 提供一些反射方面缺失功能的封装.
 */
@SuppressWarnings("unchecked")
public class BeanUtils extends org.apache.commons.beanutils.BeanUtils {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(BeanUtils.class);

	private BeanUtils() {
	}

	/**
	 * 循环向上转型,获取对象的DeclaredField.
	 * @throws NoSuchFieldException
	 *             如果没有该Field时抛出.
	 */
	public static Field getDeclaredField(Object object, String propertyName) throws NoSuchFieldException {
		Assert.notNull(object);
		Assert.hasText(propertyName);
		return getDeclaredField(object.getClass(), propertyName);
	}

	/**
	 * 循环向上转型,获取对象的DeclaredField.
	 * @throws NoSuchFieldException
	 *             如果没有该Field时抛出.
	 */
	public static Field getDeclaredField(Class clazz, String propertyName) throws NoSuchFieldException {
		Assert.notNull(clazz);
		Assert.hasText(propertyName);
		for (Class superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {
			try {
				return superClass.getDeclaredField(propertyName);
			} catch (NoSuchFieldException e) {// NOPMD
				logger.error(e.getMessage(), e);
				throw e;
			}
		}
		throw new NoSuchFieldException("No such field: " + clazz.getName() + '.' + propertyName);
	}

	/**
	 * 暴力获取对象变量值,忽略private,protected修饰符的限制.
	 * @throws NoSuchFieldException
	 *             如果没有该Field时抛出.
	 */
	public static Object forceGetProperty(Object object, String propertyName) throws NoSuchFieldException {
		Assert.notNull(object);
		Assert.hasText(propertyName);
		Field field = getDeclaredField(object, propertyName);
		boolean accessible = field.isAccessible();
		field.setAccessible(true);
		Object result = null;
		try {
			result = field.get(object);
		} catch (IllegalAccessException e) {
			logger.error(e.getMessage(), e);
		}
		field.setAccessible(accessible);
		return result;
	}

	/**
	 * 暴力设置对象变量值,忽略private,protected修饰符的限制.
	 * @throws NoSuchFieldException
	 *             如果没有该Field时抛出.
	 */
	public static void forceSetProperty(Object object, String propertyName, Object newValue)
			throws NoSuchFieldException {
		Assert.notNull(object);
		Assert.hasText(propertyName);
		Field field = getDeclaredField(object, propertyName);
		boolean accessible = field.isAccessible();
		field.setAccessible(true);
		try {
			field.set(object, newValue);
		} catch (IllegalAccessException e) {
			logger.error(e.getMessage(), e);
		}
		field.setAccessible(accessible);
	}

	/**
	 * 暴力调用对象函数,忽略private,protected修饰符的限制.
	 * @throws NoSuchMethodException
	 *             如果没有该Method时抛出.
	 */
	public static Object invokePrivateMethod(Object object, String methodName, Object... params)
			throws NoSuchMethodException {
		Assert.notNull(object);
		Assert.hasText(methodName);
		Class[] types = new Class[params.length];
		for (int i = 0; i < params.length; i++) {
			types[i] = params[i].getClass();
		}
		Class clazz = object.getClass();
		Method method = null;
		for (Class superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {
			try {
				method = superClass.getDeclaredMethod(methodName, types);
				break;
			} catch (NoSuchMethodException e) {// NOPMD
				// 方法不在当前类定义,继续向上转型
			}
		}
		if (method == null) {
			throw new NoSuchMethodException("No Such Method:" + clazz.getSimpleName() + methodName);
		}
		boolean accessible = method.isAccessible();
		method.setAccessible(true);
		Object result = null;
		try {
			result = method.invoke(object, params);
		} catch (Exception e) {
			ReflectionUtils.handleReflectionException(e);
		}
		method.setAccessible(accessible);
		return result;
	}

	/**
	 * 按Filed的类型取得Field列表.
	 */
	public static List<Field> getFieldsByType(Object object, Class type) {
		List<Field> list = new ArrayList<Field>();
		Field[] fields = object.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.getType().isAssignableFrom(type)) {
				list.add(field);
			}
		}
		return list;
	}

	/**
	 * 按FiledName获得Field的类型.
	 */
	public static Class getPropertyType(Class type, String name) throws NoSuchFieldException {
		return getDeclaredField(type, name).getType();
	}

	/**
	 * 获得field的getter函数名称.
	 */
	public static String getGetterName(Class type, String fieldName) {
		Assert.notNull(type, "Type required");
		Assert.hasText(fieldName, "FieldName required");
		if (type.getName().equals("boolean")) {
			return "is" + StringUtils.capitalize(fieldName);
		} else {
			return "get" + StringUtils.capitalize(fieldName);
		}
	}

	/**
	 * 获得field的getter函数,如果找不到该方法,返回null.
	 */
	public static Method getGetterMethod(Class type, String fieldName) {
		try {
			return type.getMethod(getGetterName(type, fieldName));
		} catch (NoSuchMethodException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public static Object invoke(String className, String methodName, Class[] argsClass, Object[] args)
			throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException, InstantiationException {
		Class cl = Class.forName(className);
		Method method = cl.getMethod(methodName, argsClass);
		return method.invoke(cl.newInstance(), args);
	}

	public static Object invoke(Object oldObject, String methodName, Class[] argsClass, Object[] args)
			throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Class cl = oldObject.getClass();
		Method method = cl.getMethod(methodName, argsClass);
		return method.invoke(oldObject, args);
	}

	public static String[] getFieldsName(Class cl) throws Exception {
		Field[] fields = cl.getDeclaredFields();
		String[] fieldNames = new String[fields.length];
		for (int i = 0; i < fields.length; i++) {
			fieldNames[i] = fields[i].getName();
		}
		return fieldNames;
	}

	public static List<String> getAllFieldName(Class cl) {// NOPMD
		List<String> list = new ArrayList<String>();
		Field[] fields = cl.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if (field.getName().equals("serialVersionUID")) {
				continue;
			}
			list.add(field.getName());
		}
		while (true) {
			cl = cl.getSuperclass();
			if (cl == Object.class) {
				break;
			}
			list.addAll(getAllFieldName(cl));
		}
		return list;
	}

	public static List<Method> getSetter(Class cl) {// NOPMD
		List<Method> list = new ArrayList<Method>();
		Method[] methods = cl.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			String methodName = method.getName();
			if (!methodName.startsWith("set")) {
				continue;
			}
			list.add(method);
		}
		while (true) {
			cl = cl.getSuperclass();
			if (cl == Object.class) {
				break;
			}
			list.addAll(getSetter(cl));
		}
		return list;
	}

	public static List<Method> getGetter(Class cl) {// NOPMD
		List<Method> list = new ArrayList<Method>();
		Method[] methods = cl.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			String methodName = method.getName();
			if (!methodName.startsWith("get") && !methodName.startsWith("is")) {
				continue;
			}
			list.add(method);
		}
		while (true) {
			cl = cl.getSuperclass();
			if (cl == Object.class) {
				break;
			}
			list.addAll(getGetter(cl));
		}
		return list;
	}

	/**
	 * Returns the classname without the package. Example: If the input class is
	 * java.lang.String than the return value is String.
	 * @param cl
	 *            The class to inspect
	 * @return The classname
	 */
	public static String getClassNameWithoutPackage(Class cl) {
		String className = cl.getName();
		int pos = className.lastIndexOf('.') + 1;
		if (pos == -1) {
			pos = 0;
		}
		return className.substring(pos);
	}

	/**
	 * 把DTO对象转成字符串
	 * @param obj
	 *            DTO对象
	 * @return 带属性名和值的字符串
	 */
	public static String beanToString(Object obj) {
		return ToStringBuilder.reflectionToString(obj);
	}
}

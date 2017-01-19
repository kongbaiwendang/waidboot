package ins.framework.multicache;

/**
 * 多key缓存
 * 
 * @author lujijiang
 * 
 * @param <T>
 */
public interface MultiCache<T> {
	/**
	 * 获取值
	 * 
	 * @param args
	 *            key序列
	 * @return
	 */
	public T get(Object... args);

	/**
	 * 设定值
	 * 
	 * @param args
	 *            key序列和值（最后一个元素）
	 * @return
	 */
	public T set(Object... args);

	/**
	 * 删除值
	 * 
	 * @param args
	 *            key序列
	 * @return
	 */
	public T del(Object... args);

	/**
	 * 判断是否存在某个key序列的值
	 * 
	 * @param args
	 * @return
	 */
	public boolean has(Object... args);

	/**
	 * 清空某个key序列之下所有子key的值
	 * 
	 * @param args
	 */
	public void clear(Object... args);

	/**
	 * 清空所有缓存
	 */
	public void clearAll();
}

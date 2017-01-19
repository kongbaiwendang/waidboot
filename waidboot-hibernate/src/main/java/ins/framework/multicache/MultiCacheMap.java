package ins.framework.multicache;

import java.util.AbstractMap;
import java.util.Set;

/**
 * 多key缓存映射
 * 
 * @author lujijiang
 * 
 * @param <T>
 */
public abstract class MultiCacheMap<T> extends AbstractMap<Object, T> {

	/**
	 * 设定值
	 * 
	 *            key序列和值（最后一个元素）
	 * @return
	 */
	public abstract T set(Object key, Object value);

	/**
	 * 删除值
	 * 
	 *            key序列
	 * @return
	 */
	public abstract T del(Object key);

	/**
	 * 判断是否存在某个key序列的值
	 * 
	 * @return
	 */
	public abstract boolean has(Object key);

	/**
	 * 清空某个key序列之下所有子key的值
	 * 
	 */
	public abstract void reset();

	/**
	 * 获取某个值
	 * 
	 * @param key
	 * @return
	 */
	public abstract T fetch(Object key);

	public boolean containsKey(Object key) {
		return has(key);
	}

	public T get(Object key) {
		return fetch(key);
	}

	public T put(Object key, T value) {
		return set(key, value);
	}

	public T remove(Object key) {
		return del(key);
	}

	public void clear() {
		reset();
	}

	public Set<java.util.Map.Entry<Object, T>> entrySet() {
		throw new UnsupportedOperationException();
	}

}

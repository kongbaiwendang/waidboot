package ins.framework.multicache;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

/**
 * 抽象的多key缓存
 * 
 * @author lujijiang
 * 
 * @param <T>
 */
public abstract class AbstractMultiCache<T> implements MultiCache<T> {
	/**
	 * 线程缓存池
	 */
	Map<Integer, MultiCacheMap<T>> cacheMap = new HashMap<Integer, MultiCacheMap<T>>();

	/**
	 * 生成一个缓存元对象
	 * 
	 * @return
	 */
	public abstract MultiCacheMap<T> createCacheMap();

	public T get(Object... args) {
		if (args == null) {
			throw new NullPointerException("the args should be null");
		}
		if (args.length < 1) {
			throw new IllegalArgumentException("the args's number at least 1");
		}
		Map<Object, T> map = getMap(copyOfRange(args, 0, args.length - 1));
		Object key = args[args.length - 1];
		return map.get(key);
	}

	private Map<Object, T> getMap(Object... args) {
		Integer size = args.length;
		Map map = cacheMap.get(size);
		if (map == null) {
			synchronized (this) {
				map = createCacheMap();
				cacheMap.put(size, (MultiCacheMap<T>) map);
			}
		}
		for (int i = 0; i < args.length; i++) {
			Map m = (Map) map.get(args[i]);
			if (m == null) {
				m = createSubMap();
				map.put(args[i], m);
			}
			map = m;
		}
		return map;
	}

	/**
	 * 创建子Map
	 * 
	 * @return
	 */
	public abstract Map createSubMap();

	public T set(Object... args) {
		if (args == null) {
			throw new NullPointerException("the args should be null");
		}
		if (args.length < 2) {
			throw new IllegalArgumentException("the args's number at least 2");
		}
		Map<Object, T> map = getMap(copyOfRange(args, 0, args.length - 2));
		Object key = args[args.length - 2];
		T value = (T) args[args.length - 1];
		return map.put(key, value);
	}

	public T del(Object... args) {
		if (args == null) {
			throw new NullPointerException("the args should be null");
		}
		if (args.length < 1) {
			throw new IllegalArgumentException("the args's number at least 1");
		}
		Map<Object, T> map = getMap(copyOfRange(args, 0, args.length - 1));
		Object key = args[args.length - 1];
		return map.remove(key);
	}

	public boolean has(Object... args) {
		if (args == null) {
			throw new NullPointerException("the args should be null");
		}
		if (args.length < 1) {
			throw new IllegalArgumentException("the args's number at least 1");
		}
		Map map = getMap(copyOfRange(args, 0, args.length - 1));
		Object key = args[args.length - 1];
		return map.containsKey(key);
	}

	public void clear(Object... args) {
		if (args == null) {
			throw new NullPointerException("the args should be null");
		}
		Map map = getMap(args);
		map.clear();
	}

	public void clearAll() {
		for (MultiCacheMap cm : cacheMap.values()) {
			cm.clear();
		}
	}

	public static <T> T[] copyOfRange(T[] original, int from, int to) {
		return copyOfRange(original, from, to, (Class<T[]>) original.getClass());
	}

	public static <T, U> T[] copyOfRange(U[] original, int from, int to,
			Class<? extends T[]> newType) {
		int newLength = to - from;
		if (newLength < 0){
			throw new IllegalArgumentException(from + " > " + to);
		};
		T[] copy = ((Object) newType == (Object) Object[].class) ? (T[]) new Object[newLength]
				: (T[]) Array.newInstance(newType.getComponentType(), newLength);
		System.arraycopy(original, from, copy, 0,
				Math.min(original.length - from, newLength));
		return copy;
	}

}

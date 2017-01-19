package ins.framework.multicache;

import java.util.HashMap;
import java.util.Map;

/**
 * 多Key缓存器
 * 
 * @author lujijiang
 * 
 */
public class Caches {
	/**
	 * 全局静态缓存
	 */
	final public static MultiCache STATIC = new AbstractMultiCache() {
		public MultiCacheMap createCacheMap() {
			return new MultiCacheMap() {
				Map map = new HashMap();

				public Object set(Object key, Object value) {
					return map.put(key, value);
				}

				public Object del(Object key) {
					return map.remove(key);
				}

				public boolean has(Object key) {
					return map.containsKey(key);
				}

				public void reset() {
					map.clear();
				}

				public Object fetch(Object key) {
					return map.get(key);
				}
			};
		}

		public Map createSubMap() {
			return new HashMap();
		}
	};
	/**
	 * 线程缓存
	 */
	final public static MultiCache THREAD = new AbstractMultiCache() {
		public MultiCacheMap createCacheMap() {
			return new MultiCacheMap() {
				ThreadLocal<Map> tl = new ThreadLocal<Map>();

				public Map getMap() {
					Map map = tl.get();
					if (map == null) {
						map = new HashMap();
						tl.set(map);
					}
					return map;
				}

				public Object set(Object key, Object value) {
					return getMap().put(key, value);
				}

				public Object del(Object key) {
					return getMap().remove(key);
				}

				public boolean has(Object key) {
					return getMap().containsKey(key);
				}

				public void reset() {
					getMap().clear();
				}

				public Object fetch(Object key) {
					return getMap().get(key);
				}
			};
		}

		public Map createSubMap() {
			return new HashMap();
		}
	};

	public static void main(String[] args) {
		THREAD.set("1", "2", "3", "value");
		THREAD.set("1", "2", "value2");
		THREAD.set("1", "value3");
		THREAD.set("2", "value4");
		Object s = THREAD.get("1", "2", "3");
		System.out.println(s);
		s = THREAD.get("1", "2");
		System.out.println(s);
		s = THREAD.get("1");
		System.out.println(s);
		s = THREAD.get("2");
		System.out.println(s);
		THREAD.set("2", "value5");
		s = THREAD.get("1", "2", "3");
		System.out.println(s);
		s = THREAD.get("1", "2");
		System.out.println(s);
		s = THREAD.get("1");
		System.out.println(s);
		s = THREAD.get("2");
		System.out.println(s);
	}
}

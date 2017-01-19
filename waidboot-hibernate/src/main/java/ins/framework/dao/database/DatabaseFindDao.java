package ins.framework.dao.database;

import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate5.HibernateTemplate;

import ins.framework.dao.database.support.Page;
import ins.framework.dao.database.support.QueryRule;

/**
 * 访问数据库的Dao接口.封装所有数据访问方法
 * 
 * @author zhouxianli
 */
public interface DatabaseFindDao  {
	

	// ======================================================================================
	// ==========普通find方法共30个，按照参数类型hql、sql、queryrule区分为3大类，前2大类10个，后1大类5个
	// ==========前2个大类下的10个find方法按照根据参数是否带entityClass分为两小类，每小类5个
	// ==========一个小类下的5个find方法按照方法名range、 all 、top、unique、 page分
	// ==========All、Top直接调用Range方法，Unique调用Range方法后加上结果条数检查，Page加上总数获取逻辑后调用Range
	// ==========不带entityClass的方法使用Object[]作为参数调用带entityClass的对应方法
	// ======================================================================================

	// =====================hql start=======================
	// ---------------------entityClass start---------------------

	public <T> List<T> findRangeByHql(final Class<T> entityClass,
			final String hql, int start, int length, final Object... values);

	public <T> List<T> findAllByHql(final Class<T> entityClass,
			final String hql, final Object... values);

	public <T> List<T> findTopByHql(final Class<T> entityClass,
			final String hql, int top, final Object... values);

	public <T> T findUniqueByHql(final Class<T> entityClass, final String hql,
			final Object... values);

	public <T> Page<T> findPageByHql(final Class<T> entityClass,
			final String hql, int pageNo, int pageSize, final Object... values);

	// ---------------------entityClass end---------------------
	// ---------------------Object[] start----------------------
	public List<Object[]> findRangeByHql(final String hql, int start,
			int length, final Object... values);

	public List<Object[]> findAllByHql(final String hql, final Object... values);

	public List<Object[]> findTopByHql(final String hql, int top,
			final Object... values);

	public Object[] findUniqueByHql(final String hql, final Object... values);

	public Page<Object[]> findPageByHql(final String hql, int pageNo,
			int pageSize, final Object... values);

	// ---------------------Object[] end----------------------
	// =====================hql end=======================

	// =====================queryRule start=======================
	// ---------------------entityClass start---------------------
	public <T> List<T> findRange(final Class<T> entityClass,
			final QueryRule queryRule, int start, int length);

	public <T> List<T> findAll(final Class<T> entityClass,
			final QueryRule queryRule);

	public <T> List<T> findTop(final Class<T> entityClass,
			final QueryRule queryRule, int top);

	public <T> T findUnique(final Class<T> entityClass, QueryRule queryRule);

	public <T> Page<T> findPage(final Class<T> entityClass,
			final QueryRule queryRule, int pageNo, int pageSize);

	// ---------------------entityClass end---------------------
	
	// =====================queryRule end=======================

	// =====================sql start=======================
	// ---------------------entityClass start---------------------

	public <T> List<T> findRangeBySql(final Class<T> entityClass,
			final String sql, int start, int length, final Object... values);

	public <T> List<T> findAllBySql(final Class<T> entityClass,
			final String sql, final Object... values);

	public <T> List<T> findTopBySql(final Class<T> entityClass,
			final String sql, int top, final Object... values);

	public <T> T findUniqueBySql(final Class<T> entityClass, final String sql,
			final Object... values);

	public <T> Page<T> findPageBySql(final Class<T> entityClass,
			final String sql, int pageNo, int pageSize, final Object... values);

	// ---------------------entityClass end---------------------
	// ---------------------Object[] start----------------------
	public List<Object[]> findRangeBySql(final String sql, int start,
			int length, final Object... values);

	public List<Object[]> findAllBySql(final String sql, final Object... values);

	public List<Object[]> findTopBySql(final String sql, int top,
			final Object... values);

	public Object[] findUniqueBySql(final String sql, final Object... values);

	public Page<Object[]> findPageBySql(final String sql, int pageNo,
			int pageSize, final Object... values);

	// ---------------------Object[] end----------------------
	// =====================sql end=======================

	// /** ====Union find 共9个=====hql、sql (X) all、 page 、top ======== */
	//
	// public <T> List<T> findAllByUnionHqls(final Class<T> entityClass,
	// final List<String> hqls, final List<List<Object>> valuess);
	//
	// public <T> Page<T> findPageByUnionHqls(final Class<T> entityClass,
	// final List<String> hqls, int pageNo, int pageSize,
	// final List<List<Object>> valuess);
	//
	// public <T> List<T> findTopByUnionHqls(final Class<T> entityClass,
	// final List<String> hqls, final int top,
	// final List<List<Object>> valuess);
	//
	// public <T> List<T> findAllByUnionSql(final Class<T> entityClass,
	// final String sql, final Object... values);
	//
	// public <T> Page<T> findPageByUnionSql(final Class<T> entityClass,
	// final String sql, int pageNo, int pageSize, final Object... values);
	//
	// public <T> List<T> findTopByUnionSql(final Class<T> entityClass,
	// final String sql, int top, final Object... values);

	/** ====特殊unique find 共2个=====map、KV ======== */

	/**
	 * 根据属性名查询出内容等于属性值的唯一对象，没符合条件的记录返回null.<br>
	 * 例如，如下语句查找id=5的唯一记录：
	 * 
	 * <pre>
	 *     &lt;code&gt;
	 * User user = service.findUnique(User.class, &quot;id&quot;, 5);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param entityClass
	 *            实体类
	 * @param propertyName
	 *            属性名
	 * @param value
	 *            属性值
	 * @return 符合条件的唯一对象 or null if not found.
	 */
	public <T> T findUniqueByKV(final Class<T> entityClass,
			final String propertyName, final Object value);

	/**
	 * 根据<属性名,属性值>的Map查询符合条件的唯一对象，没符合条件的记录返回null.<br>
	 * 例如，如下语句查找sex=1,age=18的所有记录：
	 * 
	 * <pre>
	 *     &lt;code&gt;
	 * Map properties = new HashMap();
	 * properties.put(&quot;sex&quot;, &quot;1&quot;);
	 * properties.put(&quot;age&quot;, 18);
	 * User user = service.findUnique(User.class, properties);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param entityClass
	 *            实体类
	 * @param properties
	 *            属性的Map，key为属性名，value为属性值
	 * @return 符合条件的唯一对象，没符合条件的记录返回null.
	 */
	public <T> T findUniqueByMap(final Class<T> entityClass,
			final Map<String, Object> properties);

	/**
	 * 根据查询规则查询符合条件的唯一对象，没符合条件的记录返回null.<br>
	 * 例如：
	 * 
	 * <pre>
	 *     &lt;code&gt;
	 * QueryRule queryRule = new QueryRule();
	 * queryRule.addLike(&quot;username&quot;, user.getUsername());
	 * queryRule.addLike(&quot;monicker&quot;, user.getMonicker());
	 * queryRule.addBetween(&quot;id&quot;, lowerId, upperId);
	 * User user = service.findUnique(User.class, queryRule);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param entityClass
	 *            实体类
	 * @param queryRule
	 *            查询规则
	 * @return 符合条件的唯一对象，没符合条件的记录返回null.
	 */

	/** ====lazy find 共3个=====hql、sql、queryrule ======== */
	public List findLazyByHql(final String hql, Integer pageNo, Integer pageSize,final Object... values);

	public List findLazyBySql(final String sql,Integer pageNo, Integer pageSize, final Object... values);
	
	public Page findUnionByHqls(final List<String> hqls,
			final List<List<Object>> valuess, int pageNo, int pageSize) ;
	
	
	public long getCountByHql(String hql, final Object... values);
	public HibernateTemplate getHibernateTemplateUtil();
	public Session getCurrentSession() throws DataAccessResourceFailureException;
	public void setSessionFactory(SessionFactory sessionFactory);
}

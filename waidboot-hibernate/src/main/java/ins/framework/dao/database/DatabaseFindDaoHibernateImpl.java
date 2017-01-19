package ins.framework.dao.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.internal.CriteriaImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.util.Assert;

import ins.framework.dao.database.support.Page;
import ins.framework.dao.database.support.QueryRule;
import ins.framework.dao.database.support.QueryRuleUtils;
import ins.framework.dao.database.util.InjectionCheckUtils;
import ins.framework.lang.Beans;

/**
 * DatabaseFindDao的实体基类.
 * <p/>
 * 继承于Spring的<code>HibernateDaoSupport</code> ,提供分页函数和若干便捷查询方法，并对返回值作了泛型类型转换.
 * 
 * @see HibernateDaoSupport
 * @see databaseDao
 * @author zhouxianli
 */
public class DatabaseFindDaoHibernateImpl extends HibernateDaoSupport implements
		DatabaseFindDao {
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseFindDaoHibernateImpl.class);
	private static final String CACHEABLE_KEY = "CACHEABLE_KEY";
	private static final int DEFAULT_PAGE_SIZE = 10; // 默认一页的数据量大小
	private static final ThreadLocal<Map<String, Boolean>> cacheThreadLocal = new ThreadLocal<Map<String, Boolean>>();
	private static final Pattern ORDER_PATTERN = Pattern.compile(
			"order\\s*by[\\w|\\W|\\s|\\S]*", Pattern.CASE_INSENSITIVE);
	/**
	 * 查询缓存匹配率
	 */
	static Pattern hqlQueryCacheFlagPattern = Pattern
			.compile("[\\d\\D]+?\\W((?i)(count|avg|sum|max|min))\\s*\\([\\d\\D]+?");

	/**
	 * 用于检查当前线程对应的Session是否启用查询缓存，默认启用
	 * 
	 * @param session
	 * @return
	 */
	protected boolean isCacheable(Session session) {
		String key = CACHEABLE_KEY + "|" + DatabaseFindDaoHibernateImpl.class
				+ "|" + System.identityHashCode(session);
		Map<String, Boolean> cacheMap = cacheThreadLocal.get();
		if (cacheMap == null) {
			cacheMap = new HashMap<String, Boolean>();
			cacheThreadLocal.set(cacheMap);
		}
		Boolean cacheable = cacheMap.get(key);

		return cacheable == null ? true : cacheable;
	}

	/**
	 * 用于设置当前线程对应的Session是否启用查询缓存
	 * 
	 * @param session
	 * @param cacheable
	 */
	protected void setCacheable(Session session, boolean cacheable) {
		String key = CACHEABLE_KEY + "|" + DatabaseFindDaoHibernateImpl.class
				+ "|" + System.identityHashCode(session);
		Map<String, Boolean> cacheMap = cacheThreadLocal.get();
		if (cacheMap == null) {
			cacheMap = new HashMap<String, Boolean>();
			cacheThreadLocal.set(cacheMap);
		}
		cacheMap.put(key, cacheable);
	}

	/**
	 * 用于设置当前线程对应的Session是否启用查询缓存
	 * 
	 * @param cacheable
	 */
	protected void setCacheable(boolean cacheable) {
		setCacheable(getSessionFactory().getCurrentSession(), cacheable);
	}

	/**
	 * 去除hql的select 子句，未考虑union的情况,用于pagedQuery.
	 */
	protected static String removeSelect(String hql) {
		Assert.hasText(hql); 
		int beginPos = hql.toLowerCase(Locale.US).indexOf("from");
		Assert.isTrue(beginPos != -1, " hql : " + hql
				+ " must has a keyword 'from'");
		return hql.substring(beginPos);
	}

	/**
	 * 去除hql的orderby 子句，用于pagedQuery.
	 */
	protected static String removeOrders(String hql) {
		Assert.hasText(hql); 
		Matcher m = ORDER_PATTERN.matcher(hql);
		if (m.find()) {
			return hql.substring(0, m.start());
		}
		return hql;
	}

	/**
	 * 是否包含Distinct
	 * 
	 * @return
	 */
	protected static boolean isIncludeDistinct(String hql) {
		String hqlLowerCase = hql.toLowerCase(Locale.US);
		hqlLowerCase = hqlLowerCase.replace(" ", "");
		if (hqlLowerCase.startsWith("selectdistinct")) {
			return true;
		}
		return false;
	}

	/**
	 * 拼接sql,count distinct后的结果
	 * 
	 * @param hql
	 * @return
	 */
	protected static String getDistinctCountHql(String hql) { 
		String hqlSelect = hql.toLowerCase(Locale.US).split("from")[0];
		String hqlSelectDistinct = hqlSelect.split(",")[0];
		hqlSelectDistinct = hql.substring(0, hqlSelectDistinct.length());
		String coml = hqlSelectDistinct.split("distinct")[1];
		coml = coml.replace("(", " ");
		coml = coml.replace(")", " ");
		return "select count(distinct " + coml + ")";
	}

	/**
	 * 从查询规则得到Order的List
	 * 
	 * @param queryRule
	 *            查询规则
	 * @return Order的List
	 */
	protected static List<Order> getOrderFromQueryRule(QueryRule queryRule) {
		List<Order> orders = new ArrayList<Order>();
		for (QueryRule.Rule rule : queryRule.getRuleList()) {
			switch (rule.getType()) {
			case QueryRule.ASC_ORDER:
				// propertyName非空
				if (StringUtils.isNotEmpty(rule.getPropertyName())) {
					orders.add(Order.asc(rule.getPropertyName()));
				}
				break;
			case QueryRule.DESC_ORDER:
				// propertyName非空
				if (StringUtils.isNotEmpty(rule.getPropertyName())) {
					orders.add(Order.desc(rule.getPropertyName()));
				}
				break;
			default:
				break;
			}
		}
		return orders;
	}

	/**
	 * 用于将预处理中的？号进行处理
	 * 
	 * @param ql
	 * @param values
	 * @return
	 */
	protected static String processQL(String ql, Object... values) { 
		String newQL = ql;
		int pos = 0;
		for (int i = 0; i < values.length; i++) {
			pos = newQL.indexOf('?', pos);
			if (pos == -1) {
				throw new IllegalArgumentException(
						"params and values must match.");
			}
			if (values[i] instanceof Collection) {
				newQL = newQL.substring(0, pos) + ":queryParam" + i
						+ newQL.substring(pos + 1);
			}
			pos = pos + 1;
		}

		return newQL;
	}

	public long getCountByHql(String hql, final Object... values) {
		Assert.hasText(hql, "hql must have value.");
		InjectionCheckUtils.checkValidHql(hql);
		final String fnHql = processQL(hql, values);
		// Count查询
		StringBuilder countQueryString = new StringBuilder(fnHql.length() + 20)
				.append(" select count (*) ").append(
						removeSelect(removeOrders(fnHql)));
		List<?> countList = getHibernateTemplate().find(
				countQueryString.toString(), values);
		return (Long) countList.get(0);

	}

	public long getCountBySql(String sql, final Object... values) {
		Assert.hasText(sql, "sql must have value.");
		InjectionCheckUtils.checkValidSql(sql);

		final String fnSql = processQL(sql, values);
		;
		// Count查询
		StringBuilder countQueryString = new StringBuilder(fnSql.length() + 60)
				.append(" select count (*) from ( ").append(fnSql)
				.append(") as DatabaseDao_Count");
		List<?> countList = getHibernateTemplate().find(
				countQueryString.toString(), values);
		return (Long) countList.get(0);
	}

	public long getCount(final Class<?> entityClass, final QueryRule queryRule) {

		Long count = (Long) getHibernateTemplate().execute(
				new HibernateCallback<Long>() {
					public Long doInHibernate(Session session)
							throws HibernateException {

						// Count查询
						Criteria criteria = session.createCriteria(entityClass);
						QueryRuleUtils.createCriteriaWithQueryRule(criteria,
								queryRule);
						CriteriaImpl impl = (CriteriaImpl) criteria;
						// 先把Projection和OrderBy条件取出来,清空两者来执行Count操作
						try {
							Beans.forceSetProperty(impl, "orderEntries",
									new ArrayList<String>());
						} catch (NoSuchFieldException e) {
							// Never happen
							LOGGER.debug("{}",e);
						}
						// 执行查询
						long totalCount = Long.valueOf(""
								+ criteria
										.setProjection(Projections.rowCount())
										.uniqueResult());
						return totalCount;
					}
				});

		return count;
	}

	protected Query createQuery(final String fnHql, Session session) { 
		Query query = session.createQuery(fnHql);
		if (!hqlQueryCacheFlagPattern.matcher(fnHql).matches()) {// 不是count语句才启用查询缓存
			query.setCacheable(isCacheable(session));
		} else {
			query.setCacheable(false);
			session.flush();
		}
		return query;
	}

	@Override
	public <T> List<T> findRangeByHql(final Class<T> entityClass, String hql,
			final int start, final int length, final Object... values) {

		Assert.hasText(hql, "hql must have value.");

		InjectionCheckUtils.checkValidHql(hql);
		final String fnHql = processQL(hql, values);
		// 实际查询返回分页对象
		List<T> list = getHibernateTemplate().execute(
				new HibernateCallback<List<T>>() {
					@SuppressWarnings("unchecked")
					public List<T> doInHibernate(Session session)
							throws HibernateException {
						Query query = createQuery(fnHql, session);

						for (int i = 0; i < values.length; i++) {
							if (values[i] instanceof Collection) {
								query.setParameterList("queryParam" + i,
										(Collection<?>) values[i]);
							} else {
								query.setParameter(i, values[i]);
							}
						}

						query.setFirstResult(start);
						query.setMaxResults(length);
						query.setCacheable(true);
						return query.list();
					}
				});
		return list;

	}

	@Override
	public <T> List<T> findAllByHql(final Class<T> entityClass, String hql,
			final Object... values) {
		return this.findRangeByHql(entityClass, hql, 0, Integer.MAX_VALUE,
				values);
	}

	@Override
	public <T> List<T> findTopByHql(final Class<T> entityClass, String hql,
			int top, final Object... values) {
		return this.findRangeByHql(entityClass, hql, 0, top, values);
	}

	@Override
	public <T> T findUniqueByHql(final Class<T> entityClass, String hql,
			final Object... values) {
		List<T> list = findRangeByHql(entityClass, hql, 0, 2, values);
		if (list.isEmpty()) {
			return null;
		} else if (list.size() > 1) {
			throw new IllegalStateException("findUnique return multi records.");
		}
		return list.get(0);
	}

	@Override
	public <T> Page<T> findPageByHql(final Class<T> entityClass, String hql,
			int pageNo, int pageSize, final Object... values) {
		Assert.hasText(hql);

		InjectionCheckUtils.checkValidHql(hql);
		if (pageNo <= 0) {
			pageNo = 1;
		}
		if (pageSize <= 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}

		int startIndex = Page.getStartOfPage(pageNo, pageSize);
		// 如果起始序号小于0，则直接返回。
		if (startIndex < 0) {
			return new Page<T>();
		}

		long totalCount = getCountByHql(hql, values);
		List<T> list = findRangeByHql(entityClass, hql, startIndex, pageSize,
				values);
		return new Page<T>(startIndex, totalCount, pageSize, list);
	}

	@Override
	public List<Object[]> findRangeByHql(String hql, final int start,
			final int length, final Object... values) {
		return this.findRangeByHql(Object[].class, hql, start, length, values);
	}

	@Override
	public List<Object[]> findAllByHql(String hql, final Object... values) {

		return this.findAllByHql(Object[].class, hql, values);
	}

	@Override
	public List<Object[]> findTopByHql(String hql, int top,
			final Object... values) {

		return this.findTopByHql(Object[].class, hql, top, values);
	}

	@Override
	public Object[] findUniqueByHql(String hql, final Object... values) {
		return this.findUniqueByHql(Object[].class, hql, values);
	}

	@Override
	public Page<Object[]> findPageByHql(String hql, int pageNo, int pageSize,
			final Object... values) {
		return this
				.findPageByHql(Object[].class, hql, pageNo, pageSize, values);
	}

	@Override
	public <T> List<T> findRange(final Class<T> entityClass,
			final QueryRule queryRule, final int start, final int length) {
		List<T> list = getHibernateTemplate().execute(
				new HibernateCallback<List<T>>() {
					@SuppressWarnings("unchecked")
					public List<T> doInHibernate(Session session)
							throws HibernateException {
						Criteria criteria = session.createCriteria(entityClass);
						QueryRuleUtils.createCriteriaWithQueryRule(criteria,
								queryRule);
						// 添加Order
						List<Order> orders = getOrderFromQueryRule(queryRule);
						for (Order o : orders) {
							criteria.addOrder(o);
						}
						criteria.setCacheable(isCacheable(session));
						criteria.setFirstResult(start);
						criteria.setMaxResults(length);
						return criteria.list();
					}
				});
		return list;
	}

	@Override
	public <T> List<T> findAll(final Class<T> entityClass, QueryRule queryRule) {
		return this.findRange(entityClass, queryRule, 0, Integer.MAX_VALUE);
	}

	@Override
	public <T> List<T> findTop(final Class<T> entityClass,
			final QueryRule queryRule, int top) {
		return this.findRange(entityClass, queryRule, 0, top);
	}

	@Override
	public <T> T findUnique(final Class<T> entityClass, QueryRule queryRule) {
		List<T> list = this.findRange(entityClass, queryRule, 0, 2);
		if (list.isEmpty()) {
			return null;
		} else if (list.size() > 1) {
			throw new IllegalStateException("findUnique return multi records.");
		}
		return list.get(0);
	}

	@Override
	public <T> Page<T> findPage(final Class<T> entityClass,
			final QueryRule queryRule, int pageNo, int pageSize) {
		if (pageNo <= 0) {
			pageNo = 1;
		}
		if (pageSize <= 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}

		int startIndex = Page.getStartOfPage(pageNo, pageSize);

		long totalCount = getCount(entityClass, queryRule);
		List<T> list = this.findRange(entityClass, queryRule, startIndex,
				pageSize);
		return new Page<T>(startIndex, totalCount, pageSize, list);
	}

	@Override
	public <T> List<T> findRangeBySql(final Class<T> entityClass, String sql,
			final int start, final int length, final Object... values) {
		Assert.hasText(sql, "sql must have value.");
		InjectionCheckUtils.checkValidSql(sql);
		final String fnSql = processQL(sql, values);
		List<T> list = getHibernateTemplate().execute(
				new HibernateCallback<List<T>>() {
					@SuppressWarnings("unchecked")
					public List<T> doInHibernate(Session session)
							throws HibernateException {
						SQLQuery query = session.createSQLQuery(fnSql);
						if (values != null) {
							for (int i = 0; i < values.length; i++) {
								query.setParameter(i, values[i]);
							}
						}
						query.setFirstResult(start);
						query.setMaxResults(length);
						return query.list();
					}
				});
		return list;
	}

	@Override
	public <T> List<T> findAllBySql(final Class<T> entityClass, String sql,
			final Object... values) {

		return this.findRangeBySql(entityClass, sql, 0, Integer.MAX_VALUE, values);
	}

	@Override
	public <T> List<T> findTopBySql(final Class<T> entityClass, String sql,
			int top, final Object... values) {

		return this.findRangeBySql(entityClass, sql, 0, top, values);
	}

	@Override
	public <T> T findUniqueBySql(final Class<T> entityClass, String sql,
			final Object... values) {
		List<T> list = findRangeBySql(entityClass, sql, 0, 2, values);
		if (list.isEmpty()) {
			return null;
		} else if (list.size() > 1) {
			throw new IllegalStateException("findUnique return multi records.");
		}
		return list.get(0);
	}

	@Override
	public <T> Page<T> findPageBySql(final Class<T> entityClass, String sql,
			int pageNo, int pageSize, final Object... values) {
		Assert.hasText(sql);
		InjectionCheckUtils.checkValidSql(sql);
		if (pageNo <= 0) {
			pageNo = 1;
		}
		if (pageSize <= 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}

		int startIndex = Page.getStartOfPage(pageNo, pageSize);
		// 如果起始序号小于0，则直接返回。
		if (startIndex < 0) {
			return new Page<T>();
		}

		long totalCount = getCountBySql(sql, values);
		List<T> list = findRangeBySql(entityClass, sql, startIndex, pageSize,
				values);
		return new Page<T>(startIndex, totalCount, pageSize, list);
	}

	@Override
	public List<Object[]> findRangeBySql(String sql, final int start,
			final int length, final Object... values) {

		return this.findRangeBySql(Object[].class, sql, start, length, values);
	}

	@Override
	public List<Object[]> findAllBySql(String sql, final Object... values) {

		return this.findAllBySql(Object[].class, sql, values);
	}

	@Override
	public List<Object[]> findTopBySql(String sql, int top,
			final Object... values) {

		return this.findTopBySql(Object[].class, sql, top, values);
	}

	@Override
	public Object[] findUniqueBySql(String sql, final Object... values) {

		return this.findUniqueBySql(Object[].class, sql, values);
	}

	@Override
	public Page<Object[]> findPageBySql(String sql, int pageNo, int pageSize,
			final Object... values) {
		return this
				.findPageBySql(Object[].class, sql, pageNo, pageSize, values);
	}

	@Override
	public <T> T findUniqueByKV(final Class<T> entityClass,
			String propertyName, Object value) {
		QueryRule queryRule = QueryRule.getInstance();
		queryRule.addEqual(propertyName, value);
		return findUnique(entityClass, queryRule);
	}

	@Override
	public <T> T findUniqueByMap(final Class<T> entityClass,
			Map<String, Object> properties) {
		QueryRule queryRule = QueryRule.getInstance();
		for (Iterator<String> iterator = properties.keySet().iterator(); iterator
				.hasNext();) {
			String key = (String) iterator.next();
			queryRule.addEqual(key, properties.get(key));
		}
		return findUnique(entityClass, queryRule);
	}

	@Override
	public List findLazyByHql(String hql, Integer pageNo, Integer pageSize,
			final Object... values) {

		Assert.hasText(hql);
		InjectionCheckUtils.checkValidHql(hql);
		if (pageNo <= 0) {
			pageNo = 1;
		}
		if (pageSize <= 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}

		int startIndex = Page.getStartOfPage(pageNo, pageSize);
		// 如果起始序号小于0，则直接返回。
		if (startIndex < 0) {
			return new ArrayList();
		}

		List list = findRangeByHql(hql, startIndex, pageSize, values);
		return list;

	}

	@Override
	public List findLazyBySql(String sql, Integer pageNo, Integer pageSize,
			final Object... values) {

		Assert.hasText(sql);
		InjectionCheckUtils.checkValidSql(sql);
		if (pageNo <= 0) {
			pageNo = 1;
		}
		if (pageSize <= 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}

		int startIndex = Page.getStartOfPage(pageNo, pageSize);
		// 如果起始序号小于0，则直接返回。
		if (startIndex < 0) {
			return new ArrayList();
		}

		List list = findRangeBySql(sql, startIndex, pageSize, values);
		return list;

	}

	@Override
	public Page findUnionByHqls(List<String> hqls, List<List<Object>> valuess, int pageNo, int pageSize) {
		if (pageNo <= 0) {
			pageNo = 1;
		}
		if (pageSize <= 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		// count
		Long[] counts = new Long[hqls.size()];
		long totalCount = 0;
		for (int k = 0; k < hqls.size(); k++) {
			// 替换 list的？为:queryParam + i
			String newHql = hqls.get(k);
			final Object[] values = valuess.get(k).toArray();
			int pos = 0;
			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					pos = newHql.indexOf('?', pos);
					if (pos == -1) {
						break;
					}
					if (values[i] instanceof Collection) {
						if (pos > -1) {
							newHql = newHql.substring(0, pos) + ":queryParam"
									+ i + newHql.substring(pos + 1);
						}
					}
					pos = pos + 1;
				}
			}
			final String fnHql = newHql;
			// Count查询
			final StringBuffer countQueryString = new StringBuffer(
					fnHql.length() + 20).append(" select count (*) ").append(
					removeSelect(removeOrders(fnHql)));
			List countlist = (List) getHibernateTemplate().execute(
					new HibernateCallback() {
						public Object doInHibernate(Session session)
								throws HibernateException {
							Query query = session.createQuery(countQueryString
									.toString());
							if (values != null) {
								for (int i = 0; i < values.length; i++) {
									if (values[i] instanceof Collection) {
										query.setParameterList(
												"queryParam" + i,
												(Collection) values[i]);
									} else {
										query.setParameter(i, values[i]);
									}
								}
							}
							return query.list();
						}
					});
			counts[k] = (Long) countlist.get(0);
			totalCount += counts[k];
		}// end for
			// union data
		List<List> datas = new ArrayList<List>();
		final int startIndex = Page.getStartOfPage(pageNo, pageSize);
		long gindex = 0;
		long selectCount = 0;
		for (int k = 0; k < hqls.size(); k++) {
			final String fnHql = hqls.get(k);
			final Object[] values = valuess.get(k).toArray();
			long realIndex = startIndex - gindex;
			long realSize = pageSize - selectCount;
			if (realIndex < 0) {
				realIndex = 0;
			}
			if (counts[k] - realIndex < realSize) {
				realSize = counts[k] - realIndex;
			}
			final long fnRealIndex = realIndex;
			final long fnRealSize = realSize;
			if (realIndex >= 0 && realSize > 0) {
				List list = (List) getHibernateTemplate().execute(
						new HibernateCallback() {
							public Object doInHibernate(Session session)
									throws HibernateException {
								Query query = createQuery(fnHql, session);
								if (values != null) {
									for (int i = 0; i < values.length; i++) {
										if (values[i] instanceof Collection) {
											query.setParameterList("queryParam"
													+ i, (Collection) values[i]);
										} else {
											query.setParameter(i, values[i]);
										}
									}
								}
								query.setFirstResult((int) fnRealIndex);
								query.setMaxResults((int) fnRealSize);
								return query.list();
							}
						});
				datas.add(list);
				selectCount += fnRealSize;
			}
			if (selectCount == pageSize) {
				break;
			}
			gindex += counts[k];
		}
		List resultList = new ArrayList();
		for (int i = 0; i < datas.size(); i++) {
			resultList.addAll(datas.get(i));
		}
		return new Page(startIndex, totalCount, pageSize, resultList);
	}
	
	public HibernateTemplate getHibernateTemplateUtil() {
		  return getHibernateTemplate();
		}
	
	public Session getCurrentSession() throws DataAccessResourceFailureException {
		return getSessionFactory().getCurrentSession();
	}
	public void setSessionFactoryUtil(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}
}

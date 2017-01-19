package ins.framework.dao;

import ins.framework.common.CodeValuePair;
import ins.framework.common.Page;
import ins.framework.common.QueryRule;
import ins.framework.common.QueryRule.Rule;
import ins.framework.dao.support.QueryRuleUtils;
import ins.framework.multicache.Caches;
import ins.framework.utils.BeanUtils;
import ins.framework.utils.DataUtils;
import ins.framework.utils.RequestUtils;
import ins.framework.utils.StringUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.transform.ResultTransformer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;
import org.springframework.util.Assert;

/**
 * Hibernate Dao的实体基类.
 * <p/>
 * 继承于Spring的<code>HibernateDaoSupport</code> ,提供分页函数和若干便捷查询方法，并对返回值作了泛型类型转换.
 * 
 * @see HibernateDaoSupport
 * @see GenericDaoHibernate
 * @author zhouxianli
 */
@SuppressWarnings("unchecked")
public class EntityDaoHibernate extends HibernateDaoSupport implements
		ApplicationContextAware {
	/**
	 * applicationContext,用于从中直接获取bean
	 */
	protected ApplicationContext applicationContext;
	private static final String CACHEABLE_KEY = "CACHEABLE_KEY";
	private static final int DEFAULT_PAGE_SIZE = 10; // 默认一页的数据量大小
	private static boolean optimizeFind; // 是否优化查询
	private static Map<String, String> ignoreFieldNameMapAtSaveBySql = new ConcurrentHashMap<String, String>(
			2);// 存放saveBySql时忽略的字段名
	static {
		ignoreFieldNameMapAtSaveBySql.put("inserttimeforhis",
				"InsertTimeForHis");
		ignoreFieldNameMapAtSaveBySql.put("operatetimeforhis",
				"OperateTimeForHis");
	}

	public static Map<String, String> getIgnoreFieldNameMapAtSaveBySql() {
		return ignoreFieldNameMapAtSaveBySql;
	}

	public static boolean isOptimizeFind() {
		return optimizeFind;
	}

	public static void setOptimizeFind(boolean optimizeFind) {
		EntityDaoHibernate.optimizeFind = optimizeFind;
	}

	/**
	 * 用于检查当前线程对应的Session是否启用查询缓存，默认启用
	 * 
	 * @param session
	 * @return
	 */
	protected boolean isCacheable(Session session) {
		Boolean cacheable = (Boolean) Caches.THREAD.get(CACHEABLE_KEY,
				EntityDaoHibernate.class, System.identityHashCode(session));
		return cacheable == null ? true : cacheable;
	}

	/**
	 * 用于设置当前线程对应的Session是否启用查询缓存
	 * 
	 * @param session
	 * @param cacheable
	 */
	protected void setCacheable(Session session, boolean cacheable) {
		Caches.THREAD.set(CACHEABLE_KEY, EntityDaoHibernate.class,
				System.identityHashCode(session), cacheable);
	}

	/**
	 * 用于检查当前线程对应的Session是否启用查询缓存，默认启用
	 * 
	 * @return
	 */
	protected boolean isCacheable() {
		return isCacheable(getSessionFactory().getCurrentSession());
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
	 * 根据ID获取对象. 实际调用Hibernate的session.get()方法返回实体. 如果对象不存在，返回null.<br>
	 * 例如以下代码获取主键为2的user记录
	 * 
	 * <pre>
	 * 		&lt;code&gt;
	 * User user = service.get(User.class, 2);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param entityClass
	 *            实体类
	 * @param id
	 *            序列号对象
	 * @return 匹配的对象
	 */
	public <T> T get(Class<T> entityClass, Serializable id) {
		return (T) getHibernateTemplate().get(entityClass, id);
	}

	/**
	 * 获取全部对象. <br>
	 * 例如以下代码获取user的全部记录
	 * 
	 * <pre>
	 * 		&lt;code&gt;
	 * List&lt;User&gt; users = service.getAll(User.class);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param entityClass
	 *            实体类
	 * @return 全部对象
	 */
	public <T> List<T> getAll(Class<T> entityClass) {
		return getHibernateTemplate().loadAll(entityClass);
	}

	/**
	 * 保存对象.<br>
	 * 例如：以下代码将对象保存到数据库中
	 * 
	 * <pre>
	 * 		&lt;code&gt;
	 * User entity = new User();
	 * entity.setName(&quot;zzz&quot;);
	 * // 保存对象
	 * service.save(entity);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param obj
	 *            待保存对象
	 */
	public void save(Object obj) {
		getHibernateTemplate().saveOrUpdate(obj);
	}

	/**
	 * 更新对象.<br>
	 * 例如：以下代码将对象更新到数据库中
	 * 
	 * <pre>
	 * 		&lt;code&gt;
	 * User entity = service.get(1);
	 * entity.setName(&quot;zzz&quot;);
	 * // 更新对象
	 * service.update(entity);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param obj
	 *            待更新对象
	 */
	public void update(Object obj) {
		getHibernateTemplate().update(obj);
	}

	/**
	 * 批量保存对象.<br>
	 * 例如：以下代码将对象保存到数据库中
	 * 
	 * <pre>
	 * 		&lt;code&gt;
	 * List&lt;Role&gt; list = new ArrayList&lt;Role&gt;();
	 * for (int i = 1; i &lt; 8; i++) {
	 * 	Role role = new Role();
	 * 	role.setId(i);
	 * 	role.setRolename(&quot;管理员&quot; + i);
	 * 	role.setPrivilegesFlag(&quot;1,2,3&quot;);
	 * 	list.add(role);
	 * }
	 * service.saveAll(list);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param list
	 *            待保存的对象List
	 */
	public void saveAll(List list) {
		getHibernateTemplate().saveOrUpdate(list);
	}

	/**
	 * 删除对象.<br>
	 * 例如：以下删除entity对应的记录
	 * 
	 * <pre>
	 * 		&lt;code&gt;
	 * service.remove(entity);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param obj
	 *            待删除的实体对象
	 */
	public void delete(Object obj) {
		getHibernateTemplate().delete(obj);
	}

	/**
	 * 删除对象.<br>
	 * 例如：以下删除entity对应的记录
	 * 
	 * <pre>
	 * 		&lt;code&gt;
	 * service.remove(entityList);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param entityList
	 *            待删除的实体对象列表
	 */
	public <T> void deleteAll(List entityList) {
		getHibernateTemplate().deleteAll(entityList);
	}

	/**
	 * 根据ID删除对象.如果有记录则删之，没有记录也不报异常<br>
	 * 例如：以下删除主键为1的记录
	 * 
	 * <pre>
	 * 		&lt;code&gt;
	 * service.removeByPK(User.class, 1);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param entityClass
	 *            实体类
	 * @param id
	 *            序列化对象
	 */
	public <T> void deleteByPK(Class<T> entityClass, Serializable id) {
		Object obj = get(entityClass, id);
		if (obj != null) {
			delete(obj);
		}
	}

	/**
	 * 输出缓冲区里的数据 <br>
	 * 例如：
	 * 
	 * <pre>
	 * 		&lt;code&gt;
	 * service.flush();
	 * &lt;/code&gt;
	 * </pre>
	 */
	public void flush() {
		getHibernateTemplate().flush();
	}

	/**
	 * 从一级缓存中去掉对象 <br>
	 * 例如：
	 * 
	 * <pre>
	 * 		&lt;code&gt;
	 * service.evict(user);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param obj
	 *            待清除的实体类
	 */
	public void evict(Object obj) {
		getHibernateTemplate().evict(obj);
	}

	/**
	 * 清除缓冲区里的数据<br>
	 * 例如：
	 * 
	 * <pre>
	 * 		&lt;code&gt;
	 * service.clear();
	 * &lt;/code&gt;
	 * </pre>
	 */
	public void clear() {
		getHibernateTemplate().clear();
	}

	/**
	 * 根据hql查询,查询数据.<br>
	 * <b>注意：如有可能，尽量使用 query(Class, List)。那样可以使用预处理语句处理，可以提高效率。</b> <br>
	 * 例1：以下代码查询user数据
	 * 
	 * <pre>
	 * List&lt;User&gt; list = service.findByHql(&quot;from User where name like ?&quot;, &quot;%ca%&quot;);
	 * // 显示数据
	 * for (User user : list) {
	 * 	System.out.println(ToStringBuilder.reflectionToString(user));
	 * }
	 * </pre>
	 * 
	 * 例2：以下代码查询一个区间中的Role数据，这里传入了1和15两个参数
	 * 
	 * <pre>
	 * List&lt;Role&gt; result = service.findByHql(
	 * 		&quot;from Role where id between ? and ? order by id&quot;, 1, 15);
	 * </pre>
	 * 
	 * @param hql
	 *            HQL语句
	 * @param values
	 *            可变参数
	 * @see #find(Class, QueryRule)
	 * @return 查询出的对象的List
	 */
	public List findByHql(final String hql, final Object... values) {
		Assert.hasText(hql);
		// 替换 list的？为:queryParam + i
		String newHql = hql;
		int pos = 0;
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				pos = newHql.indexOf('?', pos);
				if (pos == -1) {
					break;
				}
				if (values[i] instanceof Collection && pos > -1) {
					newHql = newHql.substring(0, pos) + ":queryParam" + i
							+ newHql.substring(pos + 1);
				}
				pos = pos + 1;
			}
		}
		final String fnHql = newHql;
		// 实际查询返回分页对象
		List list = (List) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query query = createQuery(fnHql, session);
				if (values != null) {
					for (int i = 0; i < values.length; i++) {
						if (values[i] instanceof Collection) {
							query.setParameterList("queryParam" + i,
									(Collection) values[i]);
						} else {
							query.setParameter(i, values[i]);
						}
					}
				}
				return query.list();
			}
		});
		return list;
	}

	/**
	 * 查询前top条数据,缓解数据库压力
	 * 
	 * @param hql
	 * @param top
	 * @param values
	 * @return List
	 */
	public List findTopByHql(final String hql, final int top,
			final Object... values) {
		String newHql = hql;
		int pos = 0;
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				pos = newHql.indexOf('?', pos);
				if (pos == -1) {
					break;
				}
				if (values[i] instanceof Collection) {
					if (pos > -1) {
						newHql = newHql.substring(0, pos) + ":queryParam" + i
								+ newHql.substring(pos + 1);
					}
				}
				pos = pos + 1;
			}
		}
		final String fnHql = newHql;
		// 实际查询返回分页对象
		List list = (List) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query query = createQuery(fnHql, session);
				if (values != null) {
					for (int i = 0; i < values.length; i++) {
						if (values[i] instanceof Collection) {
							query.setParameterList("queryParam" + i,
									(Collection) values[i]);
						} else {
							query.setParameter(i, values[i]);
						}
					}
				}
				query.setFirstResult(0);
				query.setMaxResults(top);
				return query.list();
			}
		});
		return list;
	}
	
	
	public static void putRecurMap(Map<String,Object> dataMap, String propertyName,Object propertyValue)
	{
		int idx=propertyName.indexOf('.');
		if(idx>0)
		{
			String leftPropertyName=propertyName.substring(0,idx);
			String rightPropertyName=propertyName.substring(idx+1);
			
			Map<String,Object> mapValue=new HashMap<String,Object>();
			putRecurMap(mapValue,rightPropertyName,propertyValue);
			dataMap.put(leftPropertyName, mapValue);			
			
		}else{
			dataMap.put(propertyName, propertyValue);
		}
	}
	
	
	/**
	 * 分页查询函数，使用sql，查询时限制条数，目前默认100条.<br>
	 * <b>注意：如有可能，尽量使用 pagedQuery(Class, List, int,
	 * int)。那样可以使用预处理语句处理，可以提高效率。</b> <br>
	 * 例如以下代码查询条件为 name like "%ca%" 的数据，每页10条记录，取第一页
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * Page page = service.findBySql(&quot;from User Where name like ?&quot;, 1, 10, &quot;%ca%&quot;);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param sql
	 *            Sql语句.
	 * @param pageNo
	 *            页号,从1开始.
	 * @param pageSize
	 *            每页的记录条数.
	 * @param values
	 *            参数值.
	 * @see #find(Class, QueryRule, int, int)
	 * @return Page 数据页
	 */
	public Page findBySqlMap(final String sql, Integer pageNo, Integer pageSize,
			final Object... values) {
		Assert.hasText(sql);
		if (pageNo <= 0) {
			pageNo = 1;
		}
		if (pageSize <= 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		// 替换 list的？为:queryParam + i
		String newSql = sql;
		int pos = 0;
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				pos = newSql.indexOf('?', pos);
				if (pos == -1) {
					break;
				}
				if (values[i] instanceof Collection) {
					if (pos > -1) {
						newSql = newSql.substring(0, pos) + ":queryParam" + i
								+ newSql.substring(pos + 1);
					}
				}
				pos = pos + 1;
			}
		}
		final String fnSql = newSql;
		final int startIndex = Page.getStartOfPage(pageNo, pageSize);
		// 如果起始序号小于0，则直接返回。
		if (startIndex < 0) {
			return new Page();
		}
		// 如果为优化查询模式且页号大于1，则不查询count值，且设置总记录数为-1。
		if (optimizeFind && pageNo > 1) {
			final int realPageSize = pageSize;
			// 实际查询返回分页对象
			List list = (List) getHibernateTemplate().execute(
					new HibernateCallback() {
						public Object doInHibernate(Session session)
								throws HibernateException {
							SQLQuery query = session.createSQLQuery(fnSql);							
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
							query.setFirstResult(startIndex);
							query.setMaxResults(realPageSize);
							query.setResultTransformer(new ResultTransformer(){
								@Override
								public Object transformTuple(
										Object[] values,
										String[] columns) {
									Map<String,Object> map=new HashMap<String,Object>();
									for(int i=0;i<columns.length;i++)
									{
										putRecurMap(map,columns[i],values[i]);
									}									
									return map;
								}

								@Override
								public List transformList(List paramList) {
									// TODO Auto-generated method stub
									return paramList;
								}								
							});
							return query.list();
						}
					});
			return new Page(startIndex, -1, pageSize, list);
		}
		// Count查询
		final int maxCount = Integer.parseInt(ins.framework.common.SysConfig.get("MaxCount"));
		String modifyHql = null;
		// 是否包含Distinct函数
//		final boolean isIncludeDistinctFlag = isIncludeDistinct(fnSql);
//		if (isIncludeDistinctFlag) {
//			modifyHql = getDistinctCountHql(fnSql)
//					+ removeSelect(removeOrders(fnSql));
//		} else {
//			modifyHql = " select 1 " + removeSelect(removeOrders(fnSql));
//		}
		modifyHql = " select 1 from (" + removeOrders(fnSql) +" ) countSQL ";
		final String countQueryString = modifyHql;
		List countList = (List) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session)
							throws HibernateException {
						SQLQuery query = session.createSQLQuery(countQueryString);
						if (values != null) {
							for (int i = 0; i < values.length; i++) {
								if (values[i] instanceof Collection) {
									query.setParameterList("queryParam" + i,
											(Collection) values[i]);
								} else {
									query.setParameter(i, values[i]);
								}
							}
						}
						query.setFirstResult(startIndex);
						query.setMaxResults(maxCount - startIndex);

						query.setResultTransformer(new ResultTransformer(){
							@Override
							public Object transformTuple(
									Object[] values,
									String[] columns) {
								Map<String,Object> map=new HashMap<String,Object>();
								for(int i=0;i<columns.length;i++)
								{
									putRecurMap(map,columns[i],values[i]);										
								}									
								return map;
							}

							@Override
							public List transformList(List paramList) {
								// TODO Auto-generated method stub
								return paramList;
							}								
						});
						return query.list();
					}
				});
		long totalCount = 0;
		
		// 判断是否有结果，没有表示无匹配数据或超过条数限制。
		if (countList.size() < 1) {
			return new Page();
		}
		// 总条数等于起始序号＋本次结果条数
		totalCount = startIndex + countList.size();
	
		final int realPageSize = pageSize;
		// 实际查询返回分页对象
		List list = (List) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				SQLQuery query = session.createSQLQuery(fnSql);
				if (values != null) {
					for (int i = 0; i < values.length; i++) {
						if (values[i] instanceof Collection) {
							query.setParameterList("queryParam" + i,
									(Collection) values[i]);
						} else {
							query.setParameter(i, values[i]);
						}
					}
				}
				
				query.setFirstResult(startIndex);
				query.setMaxResults(realPageSize);
				query.setResultTransformer(new ResultTransformer(){
					@Override
					public Object transformTuple(
							Object[] values,
							String[] columns) {
						Map<String,Object> map=new HashMap<String,Object>();
						for(int i=0;i<columns.length;i++)
						{
							putRecurMap(map,columns[i],values[i]);										
						}									
						return map;
					}

					@Override
					public List transformList(List paramList) {
						// TODO Auto-generated method stub
						return paramList;
					}								
				});
				return query.list();
			}
		});
		return new Page(startIndex, totalCount, pageSize, list);
	}
	
	
	/**
	 * 分页查询函数，使用sql，查询时限制条数，目前默认100条.<br>
	 * <b>注意：如有可能，尽量使用 pagedQuery(Class, List, int,
	 * int)。那样可以使用预处理语句处理，可以提高效率。</b> <br>
	 * 例如以下代码查询条件为 name like "%ca%" 的数据，每页10条记录，取第一页
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * Page page = service.findBySql(&quot;from User Where name like ?&quot;, 1, 10, &quot;%ca%&quot;);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param sql
	 *            Sql语句.
	 * @param pageNo
	 *            页号,从1开始.
	 * @param pageSize
	 *            每页的记录条数.
	 * @param values
	 *            参数值.
	 * @see #find(Class, QueryRule, int, int)
	 * @return Page 数据页
	 */
	public List findBySqlMapNoLimit(final String sql,final Object... values) {
		Assert.hasText(sql);
		
		List list = (List) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, HibernateException {
				SQLQuery query = session.createSQLQuery(sql);
				if (values != null) {
					for (int i = 0; i < values.length; i++) {
						query.setParameter(i, values[i]);
					}
				}
				query.setResultTransformer(new ResultTransformer(){
					@Override
					public Object transformTuple(
							Object[] values,
							String[] columns) {
						Map<String,Object> map=new HashMap<String,Object>();
						for(int i=0;i<columns.length;i++)
						{
							putRecurMap(map,columns[i],values[i]);									
						}									
						return map;
					}

					@Override
					public List transformList(List paramList) {
						// TODO Auto-generated method stub
						return paramList;
					}								
				});
				return query.list();
			}
		});
		return list;
	}
	
	
	

	/**
	 * 分页查询函数，使用hql，查询时限制条数，目前默认100条.<br>
	 * <b>注意：如有可能，尽量使用 pagedQuery(Class, List, int,
	 * int)。那样可以使用预处理语句处理，可以提高效率。</b> <br>
	 * 例如以下代码查询条件为 name like "%ca%" 的数据，每页10条记录，取第一页
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * Page page = service.findByHql(&quot;from User Where name like ?&quot;, 1, 10, &quot;%ca%&quot;);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param hql
	 *            HQL语句.
	 * @param pageNo
	 *            页号,从1开始.
	 * @param pageSize
	 *            每页的记录条数.
	 * @param values
	 *            参数值.
	 * @see #find(Class, QueryRule, int, int)
	 * @return Page 数据页
	 */
	public Page findByHql(final String hql, Integer pageNo, Integer pageSize,
			final Object... values) {
		Assert.hasText(hql);
		if (pageNo <= 0) {
			pageNo = 1;
		}
		if (pageSize <= 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		// 替换 list的？为:queryParam + i
		String newHql = hql;
		int pos = 0;
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				pos = newHql.indexOf('?', pos);
				if (pos == -1) {
					break;
				}
				if (values[i] instanceof Collection) {
					if (pos > -1) {
						newHql = newHql.substring(0, pos) + ":queryParam" + i
								+ newHql.substring(pos + 1);
					}
				}
				pos = pos + 1;
			}
		}
		final String fnHql = newHql;
		final int startIndex = Page.getStartOfPage(pageNo, pageSize);
		// 如果起始序号小于0，则直接返回。
		if (startIndex < 0) {
			return new Page();
		}
		// 如果为优化查询模式且页号大于1，则不查询count值，且设置总记录数为-1。
		if (optimizeFind && pageNo > 1) {
			final int realPageSize = pageSize;
			// 实际查询返回分页对象
			List list = (List) getHibernateTemplate().execute(
					new HibernateCallback() {
						public Object doInHibernate(Session session)
								throws HibernateException {
							Query query = createQuery(fnHql, session);
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
							query.setFirstResult(startIndex);
							query.setMaxResults(realPageSize);
							return query.list();
						}
					});
			return new Page(startIndex, -1, pageSize, list);
		}
		// Count查询
		final int maxCount = Integer.parseInt(ins.framework.common.SysConfig.get("MaxCount"));
		String modifyHql = null;
		// 是否包含Distinct函数
		final boolean isIncludeDistinctFlag = isIncludeDistinct(fnHql);
		if (isIncludeDistinctFlag) {
			modifyHql = getDistinctCountHql(fnHql)
					+ removeSelect(removeOrders(fnHql));
		} else {
			modifyHql = " select 1 " + removeSelect(removeOrders(fnHql));
		}
		final String countQueryString = modifyHql;
		List countList = (List) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session)
							throws HibernateException {
						Query query = createQuery(countQueryString, session);
						if (values != null) {
							for (int i = 0; i < values.length; i++) {
								if (values[i] instanceof Collection) {
									query.setParameterList("queryParam" + i,
											(Collection) values[i]);
								} else {
									query.setParameter(i, values[i]);
								}
							}
						}
						if (!isIncludeDistinctFlag) {
							query.setFirstResult(startIndex);
							query.setMaxResults(maxCount - startIndex);
						}
						return query.list();
					}
				});
		long totalCount = 0;
		if (isIncludeDistinctFlag) {
			totalCount = (Long) countList.get(0);
			if (totalCount < 1) {
				return new Page();
			}
		} else {
			// 判断是否有结果，没有表示无匹配数据或超过条数限制。
			if (countList.size() < 1) {
				return new Page();
			}
			// 总条数等于起始序号＋本次结果条数
			totalCount = startIndex + countList.size();
		}
		final int realPageSize = pageSize;
		// 实际查询返回分页对象
		List list = (List) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query query = createQuery(fnHql, session);
				if (values != null) {
					for (int i = 0; i < values.length; i++) {
						if (values[i] instanceof Collection) {
							query.setParameterList("queryParam" + i,
									(Collection) values[i]);
						} else {
							query.setParameter(i, values[i]);
						}
					}
				}
				query.setFirstResult(startIndex);
				query.setMaxResults(realPageSize);
				return query.list();
			}
		});
		return new Page(startIndex, totalCount, pageSize, list);
	}
	
	/**
	 * 用于延迟加载查询页，不需要查询记录总条数
	 * @param hql
	 * @param pageNo
	 * @param pageSize
	 * @param values
	 * @return
	 */
	public List lazyQuery(final String hql, Integer pageNo, Integer pageSize,
			final Object... values) {
		Assert.hasText(hql);
		if (pageNo <= 0) {
			pageNo = 1;
		}
		if (pageSize <= 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		// 替换 list的？为:queryParam + i
		String newHql = hql;
		int pos = 0;
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				pos = newHql.indexOf('?', pos);
				if (pos == -1) {
					break;
				}
				if (values[i] instanceof Collection) {
					if (pos > -1) {
						newHql = newHql.substring(0, pos) + ":queryParam" + i
								+ newHql.substring(pos + 1);
					}
				}
				pos = pos + 1;
			}
		}
		final String fnHql = newHql;
		final int startIndex = Page.getStartOfPage(pageNo, pageSize);
		// 如果起始序号小于0，则直接返回。
		if (startIndex < 0) {
			return null;
		}
		final int realPageSize = pageSize;
		// 实际查询返回分页对象
		List list = (List) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session)
							throws HibernateException {
						Query query = createQuery(fnHql, session);
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
						query.setFirstResult(startIndex);
						query.setMaxResults(realPageSize);
						return query.list();
					}
				});
		return list;
	}
	
	/**
	 * 分页查询函数，使用hql,查询时不限制条数.<br>
	 * <b>注意：如有可能，尽量使用 pagedQuery(Class, List, int,
	 * int)。那样可以使用预处理语句处理，可以提高效率。</b> <br>
	 * 例如以下代码查询条件为 name like "%ca%" 的数据，每页10条记录，取第一页
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * Page page = service.findByHqlNoLimit(&quot;from User Where name like ?&quot;, 1, 10, &quot;%ca%&quot;);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param hql
	 *            HQL语句.
	 * @param pageNo
	 *            页号,从1开始.
	 * @param pageSize
	 *            每页的记录条数.
	 * @param values
	 *            参数值.
	 * @see #find(Class, QueryRule, int, int)
	 * @return Page 数据页
	 */
	public Page findByHqlNoLimit(final String hql, int pageNo, int pageSize,
			final Object... values) {
		Assert.hasText(hql);
		if (pageNo <= 0) {
			pageNo = 1;
		}
		if (pageSize <= 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		// 替换 list的？为:queryParam + i
		String newHql = hql;
		int pos = 0;
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				pos = newHql.indexOf('?', pos);
				if (pos == -1) {
					break;
				}
				if (values[i] instanceof Collection && pos > -1) {
					newHql = newHql.substring(0, pos) + ":queryParam" + i
							+ newHql.substring(pos + 1);
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
									query.setParameterList("queryParam" + i,
											(Collection) values[i]);
								} else {
									query.setParameter(i, values[i]);
								}
							}
						}
						return query.list();
					}
				});
		long totalCount = (Long) countlist.get(0);
		if (totalCount < 1) {
			return new Page();
		}
		final int realPageSize = pageSize;
		// 实际查询返回分页对象
		final int startIndex = Page.getStartOfPage(pageNo, pageSize);
		List list = (List) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query query = createQuery(fnHql, session);
				if (values != null) {
					for (int i = 0; i < values.length; i++) {
						if (values[i] instanceof Collection) {
							query.setParameterList("queryParam" + i,
									(Collection) values[i]);
						} else {
							query.setParameter(i, values[i]);
						}
					}
				}
				query.setFirstResult(startIndex);
				query.setMaxResults(realPageSize);
				return query.list();
			}
		});
		return new Page(startIndex, totalCount, pageSize, list);
	}

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
	public <T> T findUnique(Class<T> entityClass, String propertyName,
			Object value) {
		QueryRule queryRule = QueryRule.getInstance();
		queryRule.addEqual(propertyName, value);
		List<T> list = find(entityClass, queryRule);
		if (list.isEmpty()) {
			return null;
		} else if (list.size() == 1) {
			return list.get(0);
		} else {
			if (logger.isWarnEnabled()) {
				logger.warn(StringUtils.concat("findUnique return ", list
						.size(), " record(s). EntityClass=", entityClass
						.getClass().getName(), ",propertyName=", propertyName,
						",value=", value));
			}
			throw new IllegalStateException("findUnique return " + list.size()
					+ " record(s).");
		}
	}

	/**
	 * 根据主键判断对象是否存在. 例如：以下代码判断id=2的User记录是否存在
	 * 
	 * <pre>
	 * 		&lt;code&gt;
	 * boolean user2Exist = service.exists(User.class, 2);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param entityClass
	 *            实体类
	 * @param id
	 *            序列化对象
	 * @return 存在返回true，否则返回false
	 */
	public <T> boolean exists(Class<T> entityClass, Serializable id) {
		T entity = (T) super.getHibernateTemplate().get(entityClass, id);
		if (entity == null) {
			return false;
		}
		return true;
	}

	/**
	 * 查询满足条件的记录数，使用hql.<br>
	 * 例如：查询User里满足条件 name like "%ca%" 的记录数
	 * 
	 * <pre>
	 * 		&lt;code&gt;
	 * long count = service.getCount(&quot;from User where name like ?&quot;, &quot;%ca%&quot;);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param hql
	 *            HQL语句
	 * @param values
	 *            参数List
	 * @return 满足条件的记录数
	 */
	public long getCount(String hql, Object... values) {
		Assert.hasText(hql);
		// Count查询
		StringBuffer countQueryString = new StringBuffer(hql.length() + 20)
				.append(" select count (*) ").append(
						removeSelect(removeOrders(hql)));
		List countlist = getHibernateTemplate().find(
				countQueryString.toString(), values);
		return (Long) countlist.get(0);
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
		Pattern p = Pattern.compile("order\\s*by[\\w|\\W|\\s|\\S]*",
				Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(hql);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, "");
		}
		m.appendTail(sb);
		return sb.toString();
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
	 * 查询函数，使用查询规则.<br>
	 * 例如以下代码查询条件为匹配的数据
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * QueryRule queryRule = new QueryRule();
	 * queryRule.addLike(&quot;username&quot;, user.getUsername());
	 * queryRule.addLike(&quot;monicker&quot;, user.getMonicker());
	 * queryRule.addBetween(&quot;id&quot;, lowerId, upperId);
	 * queryRule.addDescOrder(&quot;id&quot;);
	 * queryRule.addAscOrder(&quot;username&quot;);
	 * list = userService.find(User.class, queryRule);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param queryRule
	 *            查询规则
	 * @param entityClass
	 *            实体类
	 * @return 查询出的结果List
	 */
	public <T> List find(final Class<T> entityClass, final QueryRule queryRule) {
		List list = (List) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session)
							throws HibernateException {
						Criteria criteria = session.createCriteria(entityClass);
						QueryRuleUtils.createCriteriaWithQueryRule(criteria,
								queryRule);
						// 添加Order
						List<Order> orders = getOrderFromQueryRule(queryRule);
						for (Order o : orders) {
							criteria.addOrder(o);
						}
						wrapCriteria(criteria);
						return criteria.setFirstResult(0).list();
					}
				});
		return list;
	}

	/**
	 * 分页查询函数，使用查询规则.<br>
	 * 例如以下代码查询条件为匹配的数据
	 * 
	 * <pre>
	 * 	&lt;code&gt;
	 * QueryRule queryRule = new QueryRule();
	 * queryRule.addLike(&quot;username&quot;, user.getUsername());
	 * queryRule.addLike(&quot;monicker&quot;, user.getMonicker());
	 * queryRule.addBetween(&quot;id&quot;, lowerId, upperId);
	 * queryRule.addDescOrder(&quot;id&quot;);
	 * queryRule.addAscOrder(&quot;username&quot;);
	 * page = userService.find(User.class, queryRule, pageNo, pageSize);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param entityClass
	 *            实体类
	 * @param queryRule
	 *            查询规则
	 * @param pageNo
	 *            页号,从1开始.
	 * @param pageSize
	 *            每页的记录条数.
	 * @return 查询出的结果Page
	 */
	public <T> Page find(final Class<T> entityClass, final QueryRule queryRule,
			int pageNo, int pageSize) {
		if (pageNo <= 0) {
			pageNo = 1;
		}
		if (pageSize <= 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		final int realPageNo = pageNo;
		final int realPageSize = pageSize;
		// 如果为优化查询模式且页号大于1，则不查询count值，且设置总记录数为-1。
		if (optimizeFind && pageNo > 1) {
			Page page = (Page) getHibernateTemplate().execute(
					new HibernateCallback() {
						public Object doInHibernate(Session session)
								throws HibernateException {
							Criteria criteria = session
									.createCriteria(entityClass);
							QueryRuleUtils.createCriteriaWithQueryRule(
									criteria, queryRule);
							// 添加Order
							List<Order> orders = getOrderFromQueryRule(queryRule);
							for (Order o : orders) {
								criteria.addOrder(o);
							}
							int startIndex = Page.getStartOfPage(realPageNo,
									realPageSize);
							List list = fetch(criteria, startIndex,
									realPageSize);
							return new Page(startIndex, -1, realPageSize, list);
						}
					});
			return page;
		}
		Page page = (Page) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session)
							throws HibernateException {
						Criteria criteria = session.createCriteria(entityClass);
						QueryRuleUtils.createCriteriaWithQueryRule(criteria,
								queryRule);
						CriteriaImpl impl = (CriteriaImpl) criteria;
						// 先把Projection和OrderBy条件取出来,清空两者来执行Count操作
						Projection projection = impl.getProjection();
						List<CriteriaImpl.OrderEntry> orderEntries;
						try {
							orderEntries = (List) BeanUtils.forceGetProperty(
									impl, "orderEntries");
							BeanUtils.forceSetProperty(impl, "orderEntries",
									new ArrayList());
						} catch (Exception e) {
							throw new InternalError(
									" Runtime Exception impossibility throw ");
						}
						// 执行查询
						long totalCount = Long.valueOf(""
								+ criteria
										.setProjection(Projections.rowCount())
										.uniqueResult());
						// 将之前的Projection和OrderBy条件重新设回去
						criteria.setProjection(projection);
						if (projection == null) {
							criteria.setResultTransformer(CriteriaSpecification.ROOT_ENTITY);
						}
						try {
							BeanUtils.forceSetProperty(impl, "orderEntries",
									orderEntries);
						} catch (Exception e) {
							throw new InternalError(
									" Runtime Exception impossibility throw ");
						}
						// 返回分页对象
						if (totalCount < 1) {
							return new Page();
						}
						// 添加Order
						List<Order> orders = getOrderFromQueryRule(queryRule);
						for (Order o : orders) {
							criteria.addOrder(o);
						}
						int startIndex = Page.getStartOfPage(realPageNo,
								realPageSize);
						List list = fetch(criteria, startIndex, realPageSize);
						return new Page(startIndex, totalCount, realPageSize,
								list);
					}
				});
		return page;
	}

	/**
	 * 从查询规则得到Order的List
	 * 
	 * @param queryRule
	 *            查询规则
	 * @return Order的List
	 */
	protected List<Order> getOrderFromQueryRule(QueryRule queryRule) {
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
	 * 根据<属性名,属性值>的Map查询符合条件的唯一对象，没符合条件的记录返回null.
	 * 例如，如下语句查找sex=1,age=18的所有记录：
	 * 
	 * @param entityClass
	 *            实体类
	 * @param properties
	 *            属性的Map，key为属性名，value为属性值
	 * @return 符合条件的唯一对象，没符合条件的记录返回null.
	 */
	public <T> T findUnique(Class<T> entityClass, Map<String, Object> properties) {
		QueryRule queryRule = QueryRule.getInstance();
		Iterator<Entry<String, Object>> iterator = properties.entrySet().iterator(); 
		while (iterator.hasNext()) {
			Entry<String,Object> en = iterator.next();
			String key = en.getKey();
			queryRule.addEqual(key, en.getValue());
		}
		return findUnique(entityClass, queryRule);
	}

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
	public <T> T findUnique(Class<T> entityClass, QueryRule queryRule) {
		List<T> list = find(entityClass, queryRule);
		if (list.isEmpty()) {
			return null;
		} else if (list.size() == 1) {
			return list.get(0);
		} else {
			if (logger.isWarnEnabled()) {
				List<Rule> ruleList = queryRule.getRuleList();
				StringBuilder buf = new StringBuilder();
				buf.append("findUnique return ").append(list.size())
						.append(" record(s). EntityClass=")
						.append(entityClass.getClass().getName())
						.append(").append(queryRule={");
				if (ruleList != null) {
					Rule rule;
					for (int i = 0; i < ruleList.size(); i++) {
						rule = ruleList.get(i);
						if (rule != null) {
							buf.append(ToStringBuilder.reflectionToString(rule));
							if (i < ruleList.size() - 1) {
								buf.append(",");
							}
						}
					}
				}
				buf.append("}");
				logger.warn(buf.toString());
			}
			throw new IllegalStateException("findUnique return " + list.size()
					+ " record(s).");
		}
	}

	/**
	 * 根据当前list进行相应的分页返回
	 * 
	 * @param <T>
	 * @param objList
	 * @param pageNo
	 * @param pageSize
	 * @return Page
	 */
	public <T> Page pagination(List<T> objList, int pageNo, int pageSize) {
		if (pageNo <= 0) {
			pageNo = 1;
		}
		if (pageSize <= 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		List<T> objectArray = new ArrayList<T>(0);
		int startIndex = (pageNo - 1) * pageSize;
		int endIndex = pageNo * pageSize;
		for (int i = startIndex; i < endIndex; i++) {
			objectArray.add(objList.get(i));
		}
		return new Page(startIndex, objList.size(), pageSize, objectArray);
	}

	/**
	 * 合并PO List对象.(如果POJO中的值为null,则继续使用PO中的值）
	 * 
	 * @param pojoList
	 *            传入的POJO的List
	 * @param poList
	 *            传入的PO的List
	 * @param idName
	 *            ID字段名称
	 */
	public void mergeList(List pojoList, List poList, String idName) {
		mergeList(pojoList, poList, idName, false);
	}

	/**
	 * 合并PO List对象.
	 * 
	 * @param pojoList
	 *            传入的POJO的List
	 * @param poList
	 *            传入的PO的List
	 * @param idName
	 *            ID字段名称
	 * @param isCopyNull
	 *            是否拷贝null(当POJO中的值为null时，如果isCopyNull=ture,则用null,否则继续使用PO中的值）
	 */
	public void mergeList(List pojoList, List poList, String idName,
			boolean isCopyNull) {
		Map<Object, Object> map = new HashMap<Object, Object>();
		Map<Integer, Object> keyMap = new HashMap<Integer, Object>();
		Map<Object, Object> poMap = new HashMap<Object, Object>();
		// 临时存放需要删除的PO对象
		List<Object> delPoList = new ArrayList<Object>();

		for (int i = 0, count = pojoList.size(); i < count; i++) {
			Object element = pojoList.get(i);
			if (element == null) {
				continue;
			}
			Object key;
			try {
				key = PropertyUtils.getProperty(element, idName);
				map.put(key, element);
				keyMap.put(i, key);
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}
		}
		for (Iterator it = poList.iterator(); it.hasNext();) {
			Object element = (Object) it.next();
			try {
				Object key = PropertyUtils.getProperty(element, idName);
				poMap.put(key, null);
				if (!map.containsKey(key)) {
					// 暂不删除，而是保存在临时的list中，看是否能够重用
					// delete(element);
					delPoList.add(element);
					it.remove();
				} else {
					DataUtils.copySimpleObjectToTargetFromSource(element,
							map.get(key), isCopyNull);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}
		}
		for (int i = 0, count = pojoList.size(); i < count; i++) {
			Object element = pojoList.get(i);
			if (element == null) {
				continue;
			}
			Object key = keyMap.get(i);
			if (key == null) {
				// 如果主键为空，取一个要删除的po对象，进行重用，注意主键要恢复
				if (delPoList.size() > 0) {
					Object delPoObject = delPoList.get(0);
					delPoList.remove(0);

					Object delPoKey;
					try {
						// 更新对象，恢复主键
						delPoKey = PropertyUtils.getProperty(delPoObject,
								idName);
						DataUtils.copySimpleObjectToTargetFromSource(
								delPoObject, element, true);// 完全覆盖
						PropertyUtils
								.setProperty(delPoObject, idName, delPoKey);
					} catch (Exception e) {
						throw new IllegalArgumentException(e);
					}

					poList.add(delPoObject);
				} else {
					poList.add(element);
				}
			} else if (!poMap.containsKey(key)) {
				poList.add(element);
			}
		}

		// 删除真正需要删除的PO对象
		for (Object delPo : delPoList) {
			delete(delPo);
		}
	}

	/**
	 * 获取序列号
	 * 
	 * @param sequenceName
	 * @return 序列号值
	 */
	public Long getSequence(final String sequenceName) {
		Long seq = (Long) getHibernateTemplate().execute(
				new HibernateCallback() {
					public Object doInHibernate(Session session)
							throws HibernateException {
						SQLQuery sqlQuery = session.createSQLQuery("select "
								+ sequenceName
								+ ".nextval from systables where tabid=1");
						List list = sqlQuery.list();
						return Long.valueOf(list.get(0).toString());
					}
				});
		return seq;
	}

	/**
	 * （谨慎使用）直接SQL查询Union语句(参数值用问号替代,如此可以使用预处理语句以提高性能)<br>
	 * 
	 * @param sql
	 *            SQL语句(参数值用问号替代),一定要包含union
	 * @param values
	 *            参数，支持变参
	 * @return 查询返回结果
	 */
	public List findUnionBySql(final String sql, final Object... values) {
		Assert.hasText(sql);
		Assert.isTrue(sql.toLowerCase(Locale.US).indexOf("union") != -1);
		List list = (List) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, HibernateException {
				SQLQuery query = session.createSQLQuery(sql);
				if (values != null) {
					for (int i = 0; i < values.length; i++) {
						query.setParameter(i, values[i]);
					}
				}
				return query.list();
			}
		});
		return list;
	}

	/**
	 * 连接多个hql查询结果分页
	 * 
	 * @param hqls
	 *            HQL查询语句的List对象
	 * @param valuess
	 *            查询参数
	 * @param pageNo
	 *            页码
	 * @param pageSize
	 *            一页记录数
	 * @return 查询返回结果
	 */
	public Page findUnionByHqls(final List<String> hqls,
			final List<List<Object>> valuess, int pageNo, int pageSize) {
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

	/**
	 * 连接多个hql查询Top结果
	 * 
	 * @param hqls
	 *            HQL查询语句的List对象
	 * @param top
	 *            top大小
	 * @param valuess
	 *            查询参数
	 * @return Top结果
	 */
	public Page findTopUnionByHqls(final List<String> hqls, final int top,
			final List<List<Object>> valuess) {
		// union data
		List<List> datas = new ArrayList<List>();
		int selectCount = 0;
		for (int i = 0; i < hqls.size(); i++) {
			final String fnHql = hqls.get(i);
			final Object[] values = valuess.get(i).toArray();
			final int maxCount = top - selectCount;
			List list = (List) getHibernateTemplate().execute(
					new HibernateCallback() {
						public Object doInHibernate(Session session)
								throws HibernateException {
							Query query = createQuery(fnHql, session);
							if (values != null) {
								for (int j = 0; j < values.length; j++) {
									if (values[j] instanceof Collection) {
										query.setParameterList(
												"queryParam" + j,
												(Collection) values[j]);
									} else {
										query.setParameter(j, values[j]);
									}
								}
							}
							query.setMaxResults(maxCount);
							return query.list();
						}
					});
			datas.add(list);
			selectCount += list.size();
			if (selectCount >= top) {
				break;
			}
		}
		List resultList = new ArrayList();
		for (int i = 0; i < datas.size(); i++) {
			resultList.addAll(datas.get(i));
		}
		return new Page(1, selectCount, selectCount, resultList);
	}

	/**
	 * 根据sql查询,查询数据.<br>
	 * 例1：以下代码查询user数据
	 * 
	 * <pre>
	 * List&lt;User&gt; list = service.findByHql(
	 * 		&quot;select * from Demo_Type o where o.name = ? for update&quot;, &quot;Age&quot;);
	 * // 显示数据
	 * for (User user : list) {
	 * 	System.out.println(ToStringBuilder.reflectionToString(user));
	 * }
	 * </pre>
	 * 
	 * @param sql
	 *            SQL语句(注意：查询的是表而不是对象)
	 * @param values
	 *            可变参数
	 * @return 查询出的对象的List
	 */
	public List findBySql(final String sql, final Object... values) {
		Assert.hasText(sql);
		List list = (List) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				SQLQuery query = session.createSQLQuery(sql);
				if (values != null) {
					for (int i = 0; i < values.length; i++) {
						query.setParameter(i, values[i]);
					}
				}
				return query.list();
			}
		});
		return list;
	}

	/**
	 * 用SQL方式插入对象进数据库
	 * 
	 * @param pojo
	 *            pojo对象
	 * @deprecated 请使用replicate
	 */
	public void saveBySql(Object pojo) {
		try {
			Map<String, Object> savedMap = new HashMap<String, Object>();
			saveBySqlInner(pojo, savedMap);
			this.getHibernateTemplate().merge(pojo);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static Method findPojoIdMethod(Object pojo) {
		Class cls = pojo.getClass();
		List<Method> getterMethodList = BeanUtils.getGetter(cls);
		for (int i = 0; i < getterMethodList.size(); i++) {
			Method method = getterMethodList.get(i);
			Object annoObj = method.getAnnotation(Id.class);
			if (annoObj != null) {
				return method;
			}
		}
		return null;
	}

	private void saveBySqlInner(Object pojo, Map<String, Object> savedMap)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Class cls = pojo.getClass();
		Object annoObj = cls.getAnnotation(Table.class);
		if (annoObj == null) {
			return;
		}
		// 表名取元数据信息
		String tableName = ((Table) annoObj).name();
		List<Method> getterMethodList = BeanUtils.getGetter(cls);
		StringBuilder sql = new StringBuilder();
		sql.append("insert into ").append(tableName).append("(");
		int fieldCount = 0;
		Class[] zeroClass = new Class[0];
		Object[] zeroObject = new Object[0];
		List<Object> valueList = new ArrayList<Object>();
		List<Method> todoGetMethodList = new ArrayList<Method>(); // 待处理对象的get方法列表
		Map<String, String> fieldNameMap = new HashMap<String, String>();// 存放字段名（发现有重复的）
		Object id = null;
		for (int i = 0; i < getterMethodList.size(); i++) {
			Method method = getterMethodList.get(i);
			annoObj = method.getAnnotation(Column.class);
			String fieldName = "";
			Object value = null;
			if (annoObj == null) {
				annoObj = method.getAnnotation(JoinColumn.class);
				if (annoObj == null) {
					todoGetMethodList.add(method);
					continue;
				}
				// 外键字段
				fieldName = ((JoinColumn) annoObj).name();
				if (fieldName == null) {
					continue;
				}
				if (ignoreFieldNameMapAtSaveBySql.containsKey(fieldName
						.toLowerCase())) {
					continue;
				}
				if (fieldNameMap.containsKey(fieldName)) {
					continue;
				}
				fieldNameMap.put(fieldName, fieldName);
				// 外键，取父对象的ID值
				Object parentObject = method.invoke(pojo, zeroObject);
				value = findPojoIdMethod(parentObject).invoke(parentObject,
						zeroClass);

			} else {
				// 普通字段
				fieldName = ((Column) annoObj).name();
				if (fieldName == null) {
					continue;
				}
				if (ignoreFieldNameMapAtSaveBySql.containsKey(fieldName
						.toLowerCase())) {
					continue;
				}
				if (fieldNameMap.containsKey(fieldName)) {
					continue;
				}
				annoObj = method
						.getAnnotation(org.hibernate.annotations.Type.class);
				if (annoObj != null) {
					if ("org.springframework.orm.hibernate3.support.ClobStringType"
							.equals(((org.hibernate.annotations.Type) annoObj)
									.type())) {
						continue;
					}
				}
				fieldNameMap.put(fieldName, fieldName);
				// 普通属性，直接取值
				value = method.invoke(pojo, zeroObject);
				if (id == null && method.getAnnotation(Id.class) != null) {
					id = value;
				}
			}
			if (value == null) {
				continue;
			}
			valueList.add(value);
			sql.append(fieldName).append(",");
			fieldCount++;
		}
		// 将最后一个“，”替换成为“）”
		sql.setCharAt(sql.length() - 1, ')');
		sql.append(" values(");
		for (int i = 0; i < fieldCount; i++) {
			sql.append("?,");
		}
		// 将最后一个“，”替换成为“）”
		sql.setCharAt(sql.length() - 1, ')');
		// 检查是否重复
		String key = tableName + "|" + id;
		if (savedMap.containsKey(key)) {
			return;
		}
		savedMap.put(key, id);
		// 构建查询
		SQLQuery query = super.currentSession().createSQLQuery(sql.toString());

		// 给参数赋值
		for (int i = 0; i < fieldCount; i++) {
			query.setParameter(i, valueList.get(i));
		}
		query.executeUpdate();

		// 本身执行完毕，下面处理子对象
		for (int i = 0; i < todoGetMethodList.size(); i++) {
			Method method = todoGetMethodList.get(i);
			annoObj = method.getAnnotation(OneToMany.class);
			if (annoObj != null) {
				// 处理一对多的子对象
				List value = (List) method.invoke(pojo, zeroClass);
				for (int j = 0; j < value.size(); j++) {
					saveBySqlInner(value.get(j), savedMap);
				}
			}
			annoObj = method.getAnnotation(PrimaryKeyJoinColumn.class);
			if (annoObj != null) {
				// 处理单一主键的一对一子对象
				Object value = method.invoke(pojo, zeroClass);
				if (value != null) {
					saveBySqlInner(value, savedMap);
				}
			}
		}
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	protected Query createQuery(final String fnHql, Session session) {
		Query query = session.createQuery(fnHql);
		query.setCacheable(isCacheable());
		return query;
	}

	protected List fetch(Criteria criteria, int startIndex,
			final int realPageSize) {
		wrapCriteria(criteria);
		return criteria.setFirstResult(startIndex).setMaxResults(realPageSize)
				.list();
	}

	protected void wrapCriteria(Criteria criteria) {
		criteria.setCacheable(isCacheable());
	}
	
	/**
	 * 获取排序list
	 * @param request
	 * @return
	 */
	public List<CodeValuePair> genOrderByList(Map<String, String[]> paramMap){

		// 获取传入查询条件
		Map<String, String> processedCondMap = new HashMap<String, String>();// 已处理的参数
		List<CodeValuePair> columnList = new ArrayList<CodeValuePair>();
		List<CodeValuePair> orderByList = new ArrayList<CodeValuePair>();
		String code;
		String value;
		int pos;
		String[] keys = new String[paramMap.size()];
		paramMap.keySet().toArray(keys);
		List<String> list = Arrays.asList(keys);
		List<String> columnsDataList = new ArrayList<String>();
		List<String> orderColumnList = new ArrayList<String>();
		List<String> orderDirList = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			String key = list.get(i);
			if (key.startsWith("columns[") && key.endsWith("][data]")) {
				columnsDataList.add(key);
			} else if (key.startsWith("order[") && key.endsWith("][column]")) {
				orderColumnList.add(key);
			} else if (key.startsWith("order[") && key.endsWith("][dir]")) {
				orderDirList.add(key);
			}
		}
		
		Collections.sort(columnsDataList,new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				String codeO1 = o1.substring("columns[".length());
				int posO1 = codeO1.indexOf(']');
				int numO1 = Integer.valueOf(codeO1.substring(0, posO1));
				
				String codeO2 = o2.substring("columns[".length());
				int posO2 = codeO2.indexOf(']');
				int numO2 = Integer.valueOf(codeO2.substring(0, posO2));
				
				if(numO1 > numO2){
					return 1;
				}else if(numO1 < numO2){
					return -1;
				}
				return 0;
			}
		});
		Collections.sort(orderColumnList,new Comparator<String>() {
			
			@Override
			public int compare(String o1, String o2) {
				String codeO1 = o1.substring("order[".length());
				int posO1 = codeO1.indexOf(']');
				int numO1 = Integer.valueOf(codeO1.substring(0, posO1));
				
				String codeO2 = o2.substring("order[".length());
				int posO2 = codeO2.indexOf(']');
				int numO2 = Integer.valueOf(codeO2.substring(0, posO2));
				
				if(numO1 > numO2){
					return 1;
				}else if(numO1 < numO2){
					return -1;
				}
				return 0;
			}
		});
		Collections.sort(orderDirList,new Comparator<String>() {
			
			@Override
			public int compare(String o1, String o2) {
				String codeO1 = o1.substring("order[".length());
				int posO1 = codeO1.indexOf(']');
				int numO1 = Integer.valueOf(codeO1.substring(0, posO1));
				
				String codeO2 = o2.substring("order[".length());
				int posO2 = codeO2.indexOf(']');
				int numO2 = Integer.valueOf(codeO2.substring(0, posO2));
				
				if(numO1 > numO2){
					return 1;
				}else if(numO1 < numO2){
					return -1;
				}
				return 0;
			}
		});
		
		for(String columnsData : columnsDataList){
			value = RequestUtils.getString(paramMap, columnsData, "");
			code = columnsData.substring("columns[".length());
			pos = code.indexOf(']');
			code = code.substring(0, pos);
			columnList.add(new CodeValuePair(code, value));
		}
		
		for(String orderColumn : orderColumnList){
			value = RequestUtils.getString(paramMap, orderColumn, "");
			value = columnList.get(Integer.valueOf(value)).getValue();
			orderByList.add(new CodeValuePair(value, ""));
		}
		
		for(String orderDir : orderDirList){
			value = RequestUtils.getString(paramMap, orderDir, "");
			code = orderDir.substring("order[".length());
			pos = code.indexOf(']');
			code = code.substring(0, pos);
			if(!"".equals(orderByList.get(Integer.valueOf(code)).getCode())){
				orderByList.get(Integer.valueOf(code)).setValue(value);
			}else{
				orderByList.remove(Integer.valueOf(code));
			}
		}
		processedCondMap.clear();

		if (orderByList.size() > 0) {
			 return orderByList;
		}
		return null;
	
	}
	
	/**
	 * 将排序加到queryRule中
	 * @param queryRule
	 * @return
	 */
	public QueryRule getOrderBy(Map<String, String[]> paramMap,QueryRule queryRule){
		List<CodeValuePair> orderByList = this.genOrderByList(paramMap);
		if (orderByList.size() > 0) {
			for (int i = 0; i < orderByList.size(); i++) {
				CodeValuePair codeValuePair = orderByList.get(i);
				if (StringUtils.isNotBlank(codeValuePair.getCode())) {
					if("asc".equals(codeValuePair.getValue())){
						queryRule.addAscOrder(codeValuePair.getCode());
					}else{
						queryRule.addDescOrder(codeValuePair.getCode());
					}
				}
			}
		}
		return queryRule;
	}
	
	public String getOrderByString(Map<String, String[]> paramMap){
		List<CodeValuePair> orderByList = this.genOrderByList(paramMap);
		if (orderByList.size() > 0) {
			StringBuilder orderByBuf = new StringBuilder();
			int useOrderByCount = 0;
			for (int i = 0; i < orderByList.size(); i++) {
				CodeValuePair codeValuePair = orderByList.get(i);
				if (StringUtils.isNotBlank(codeValuePair.getCode())) {
					orderByBuf.append(codeValuePair.getCode())
							.append(" ").append(codeValuePair.getValue());
					orderByBuf.append(',');
					useOrderByCount++;
				}
			}

			if (useOrderByCount > 0) {
				return orderByBuf.substring(0, orderByBuf.length() - 1);
			}
		}
		return "";
	}
	
	
}

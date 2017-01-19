package ins.framework.dao.database;

import java.io.Serializable;
import java.util.List;

/**
 * 访问数据库的Dao接口.封装所有数据访问方法
 * 
 * @author zhouxianli
 */
public interface DatabaseDao extends DatabaseFindDao {
	 
	/**
	 * 获取序列号
	 * 
	 * @param sequenceName 
	 * @return 序列号值
	 */
	public Long getSequence(final String sequenceName);

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
	public long getCount(final String hql,final Object... values);
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
	public void clear();
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
	public void flush();
	
	/**
	 * 根据ID获取对象. 实际调用Hibernate的session.get()方法返回实体. 如果对象不存在，返回null.<br>
	 * 例如以下代码获取主键为2的user记录
	 * 
	 * <pre>
	 * 		&lt;code&gt;
	 * User user = service.findByPK(User.class, 2);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param entityClass
	 *            实体类
	 * @param id
	 *            序列号对象
	 * @return 匹配的对象
	 */
	public <T> T findByPK(final Class<T> entityClass, final Serializable id);

	/**
	 * 获取全部对象. <br>
	 * 例如以下代码获取user的全部记录
	 * 
	 * <pre>
	 * 		&lt;code&gt;
	 * List&lt;User&gt; users = service.findAll(User.class);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param entityClass
	 *            实体类
	 * @return 全部对象
	 */
	public <T> List<T> findAll(final Class<T> entityClass);


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
	public <T> Serializable save(final Class<T> entityClass, final Object obj);

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
	public <T> void update(final Class<T> entityClass, final Object obj);

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
	public <T> void saveAll(final Class<T> entityClass, final List<T> list);

	/**
	 * 删除对象.<br>
	 * 例如：以下删除entity对应的记录
	 * 
	 * <pre>
	 * 		&lt;code&gt;
	 * service.deleteByObject(entity);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param obj
	 *            待删除的实体对象
	 */
	public <T> void deleteByObject(final Class<T> entityClass, final Object obj);

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
	public <T> void deleteAll(final Class<T> entityClass,
			final List<T> entityList);

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
	public <T> void deleteByPK(final Class<T> entityClass, final Serializable id);

	/**
	 * 从一级缓存中去掉对象 <br>
	 * 例如：
	 * 
	 * <pre>
	 * 		&lt;code&gt;
	 * service.evictObject(user);
	 * &lt;/code&gt;
	 * </pre>
	 * 
	 * @param obj
	 *            待清除的实体类
	 */
	public <T> void evictObject(final Class<T> entityClass, final Object obj);

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
	public <T> boolean exists(final Class<T> entityClass, final Serializable id);

}

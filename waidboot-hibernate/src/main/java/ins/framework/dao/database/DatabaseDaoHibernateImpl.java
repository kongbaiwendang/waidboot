package ins.framework.dao.database;

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.util.Assert;

import ins.framework.dao.database.util.InjectionCheckUtils;

/**
 * Hibernate Dao的实体基类.
 * <p/>
 * 继承于Spring的<code>HibernateDaoSupport</code> ,提供分页函数和若干便捷查询方法，并对返回值作了泛型类型转换.
 * 
 * @see HibernateDaoSupport
 * @see databaseDao
 * @author zhouxianli
 */
public class DatabaseDaoHibernateImpl extends DatabaseFindDaoHibernateImpl
		implements DatabaseDao {

	@Override
	public Long getSequence(final String sequenceName) {
		Assert.hasText(sequenceName, "sequenceName must have value.");
		InjectionCheckUtils.checkValidName(sequenceName);
		Long seq = getHibernateTemplate().execute(
				new HibernateCallback<Long>() {
					public Long doInHibernate(Session session)
							throws HibernateException {
						SQLQuery sqlQuery = session.createSQLQuery("select "
								+ sequenceName
								+ ".nextval from systables where tabid=1");
						List<?> list = sqlQuery.list();
						return Long.valueOf(list.get(0).toString());
					}
				});
		return seq;
	}

	@Override
	public long getCount(final String hql, final Object... values) {
		Assert.hasText(hql, "getCount hql must have value.");
		InjectionCheckUtils.checkValidHql(hql);
		// Count查询
		StringBuilder countQueryString = new StringBuilder(hql.length() + 20)
				.append(" select count (*) ").append(
						removeSelect(removeOrders(hql)));
		List<?> countList = getHibernateTemplate().find(
				countQueryString.toString(), values);
		return (Long) countList.get(0);
	}

	@Override
	public void clear() {
		getHibernateTemplate().clear();

	}

	@Override
	public void flush() {
		getHibernateTemplate().flush();

	}

	@Override
	public <T> T findByPK(final Class<T> entityClass, final Serializable id) {
		return getHibernateTemplate().get(entityClass, id);
	}

	@Override
	public <T> List<T> findAll(final Class<T> entityClass) {
		return getHibernateTemplate().loadAll(entityClass);
	}

	@Override
	public <T> Serializable save(final Class<T> entityClass, final Object obj) {
		return getHibernateTemplate().save(obj); 
	}

	@Override
	public <T> void update(final Class<T> entityClass, final Object obj) {
		getHibernateTemplate().update(obj);
	}

	@Override
	public <T> void saveAll(final Class<T> entityClass, final List<T> list) {
		for (Object object : list) {
			getHibernateTemplate().merge(object);
		}
	}

	@Override
	public <T> void deleteByObject(final Class<T> entityClass, final Object obj) {
		getHibernateTemplate().delete(obj);
	}

	@Override
	public <T> void deleteAll(final Class<T> entityClass,
			final List<T> entityList) {

		getHibernateTemplate().deleteAll(entityList);

	}

	@Override
	public <T> void deleteByPK(final Class<T> entityClass, final Serializable id) {
		Object obj = findByPK(entityClass, id);
		if (obj != null) {
			deleteByObject(entityClass, obj);
		}

	}

	@Override
	public <T> void evictObject(final Class<T> entityClass, final Object obj) {
		getHibernateTemplate().evict(obj);
	}

	@Override
	public <T> boolean exists(final Class<T> entityClass, Serializable id) {
		Object entity = findByPK(entityClass, id);
		if (entity == null) {
			return false;
		}
		return true;
	}
}

package ins.framework.common;
import ins.framework.dao.EntityDaoHibernate;

import java.util.AbstractList;
import java.util.List;


public class LazyPage extends AbstractList{
	
    public static final int DEFAULT_SIZE = 25;
    EntityDaoHibernate dao;
    int pageNo;
    int pageSize;
    List cacheList;
    String hql;
    int cachePageNo;
    String[] args;

    /**
     * 默认从第一页开始，每页25条记录
     * @param dao
     * @param hql
     * @param args
     */
    public LazyPage(EntityDaoHibernate dao,String hql,String... args)
    {
        this(dao,hql,1,DEFAULT_SIZE,args);
    }
    
    /**
     * 默认从第一页开始，可以自定义每页条数
     * @param dao
     * @param hql
     * @param pageSize
     * @param args
     */
    public LazyPage(EntityDaoHibernate dao,String hql,int pageSize,String... args)
    {
    	this(dao, hql, 1, pageSize, args);
    }
    
    /**
     * 可以自定义起始页和每页条数
     * @param dao
     * @param hql
     * @param pageNo
     * @param pageSize
     * @param args
     */
    public LazyPage(EntityDaoHibernate dao,String hql,int pageNo,int pageSize,String... args)
    {
    	this.dao = dao;
    	this.hql = hql;
    	setPageNo(pageNo);
    	setPageSize(pageSize);
    	this.args = args;
    }
    
	/**
	 * 获取当前页
	 * 
	 * @return
	 */
	public int getPageNo() {
		return pageNo;
	}
    
	/**
	 * 设置当前页
	 * 
	 * @param pageNo
	 * @return
	 */
	public LazyPage setPageNo(int pageNo) {
		pageNo = pageNo < 1 ? 1 : pageNo;
		this.pageNo = pageNo;
		return this;
	}
	
	/**
	 * 获取每页记录数
	 * 
	 * @return
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * 设置每页记录数
	 * 
	 * @param pageSize
	 */
	public void setPageSize(int pageSize) {
		if (pageSize < 1) {
			throw new IllegalArgumentException(
					"The pageSize can't be less than one");
		}
		this.pageSize = pageSize;
	}
	
	@Override
	public Object get(int index) {
		cachePageNo = index/DEFAULT_SIZE + 1;
		if(cachePageNo != pageNo){
			pageNo = cachePageNo;
			cacheList = null;
		}
		if(cacheList == null){
			cacheList = dao.lazyQuery(hql,pageNo,pageSize,args);
		}
		index = index % DEFAULT_SIZE;
		return cacheList.get(index);
	}

	/**
	 * 当前缓存条数
	 */
	@Override
	public int size() {
		if(cacheList == null){
			return 0;
		}else{
			return cacheList.size();
		}
	}
	
	/**
	 * 总数
	 * @return
	 */
	public int getTotalCount() {
		return Integer.parseInt(dao.getCount(hql,args) + "");
	}

}

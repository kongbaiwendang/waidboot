package ins.platform.sysuser.service.spring;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ins.framework.dao.database.DatabaseDao;
import ins.framework.service.AbstractBaseCrudServiceSpringImpl;
import ins.platform.schema.TestDirty;
import ins.platform.sysuser.service.facade.TestDirtyService;
import ins.platform.vo.TestDirtyVo;

@Service(value = "testDirtyService")
public class TestDirtyServiceSpringImpl extends 
AbstractBaseCrudServiceSpringImpl<TestDirty, Long, TestDirtyVo, Long> implements TestDirtyService{

	
	@Autowired
	private DatabaseDao databaseDao;
	
	public static SessionFactory sessionFactory;
	

	/**
	 * 采用默认事物配置
	 * 
	 * @author WANGYADONG
	 */
	@Override
	@Transactional(propagation=Propagation.REQUIRED,readOnly=false)
	public void batchSave() {
		for(int i=1;i<100;i++){
			TestDirtyVo testDirty = new TestDirtyVo();
			testDirty.setDataName("数据保存："+i);
			System.out.println("时间戳："+System.currentTimeMillis()+"，线程ThreadInsert执行数据保存，第"+i+"条。");
//			databaseDao.save(TestDirty.class, testDirty);
			this.save(testDirty);
			long num = this.singleCount("batchSave");
			System.out.println("时间戳："+System.currentTimeMillis()+"，线程ThreadInsert读取TestDirty总数："+num);
//			String str = null;
//			if(i==50){
//				str.substring(0, 9);
//			}
			try {
				Thread.sleep(1000l);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 查询采用脏读配置，注意，如果xml与注解同时存在，则注解失效
	 * 
	 * @author WANGYADONG
	 */
	@Override
	@Transactional(propagation=Propagation.REQUIRED,isolation=Isolation.READ_UNCOMMITTED,readOnly=true)
	public long singleCount(String name) {
		long num = databaseDao.getCountByHql("from TestDirty", new Object[]{});
		return num;
	}
}

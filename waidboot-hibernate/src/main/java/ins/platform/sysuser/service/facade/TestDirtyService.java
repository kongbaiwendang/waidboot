package ins.platform.sysuser.service.facade;

import ins.framework.service.BaseCrudService;
import ins.platform.vo.TestDirtyVo;

public interface TestDirtyService extends BaseCrudService<TestDirtyVo, Long>{
	
	public void batchSave();
	
	
	public long singleCount(String name);
}

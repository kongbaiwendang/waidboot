package ins.platform.vo;

import java.io.Serializable;

public class TestDirtyVo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	
	private Long id;
	private String dataName;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getDataName() {
		return dataName;
	}
	public void setDataName(String dataName) {
		this.dataName = dataName;
	}
	
}

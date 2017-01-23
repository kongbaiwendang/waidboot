package ins.framework.mybatis.generator;

import lombok.Data;

@Data
class GenFileInfo {
	private String name;
	private String packageName;
	private String path;

	public GenFileInfo(String name, String packageName, String path) {
		super();
		this.name = name;
		this.packageName = packageName;
		this.path = path;
	}

}

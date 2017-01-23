package ins.framework.mybatis.generator;

import lombok.Data;

@Data
class Column {
	private String name;
	private String type;
	private int size;
	private String defaultValue;
	private String comment;
	private boolean nullable;
	

}

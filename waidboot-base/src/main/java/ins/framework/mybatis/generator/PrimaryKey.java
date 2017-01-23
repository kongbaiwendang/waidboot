package ins.framework.mybatis.generator;

import lombok.Data;

@Data
class PrimaryKey {
	private String pkName;
	private int keySeq;
	private String columnName;
}

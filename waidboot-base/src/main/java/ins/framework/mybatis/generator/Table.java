package ins.framework.mybatis.generator;

import java.util.List;

import lombok.Data;

@Data
class Table {
	private String name;
	private String comment;
	private List<Column> columns;
	private List<PrimaryKey> primaryKeys;
}

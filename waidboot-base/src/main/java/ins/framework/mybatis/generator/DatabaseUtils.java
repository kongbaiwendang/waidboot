package ins.framework.mybatis.generator;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库元数据解析工具类
 * 
 * @author zhouxianli
 *
 */
class DatabaseUtils {
	private Connection conn = null; 
	private String schema;
	Database database = null;

	/**
	 * 禁止实例化
	 */
	private DatabaseUtils() {

	}

	public static DatabaseUtils getInstance(Connection  conn,String schema) {
		DatabaseUtils obj = new DatabaseUtils();
		try {
			 
			DatabaseMetaData metaData = conn.getMetaData();
			Database database = new Database();
			database.setProductName(metaData.getDatabaseProductName());
			database.setProductVersion(metaData.getDatabaseProductVersion());
			obj.conn = conn;
			obj.database = database;
			obj.schema=schema;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}

	public Map<String, String> getAllTableNamesMap() {
		ResultSet rs = null;
		Map<String, String> result = new HashMap<String, String>();
		try { 
			DatabaseMetaData metaData = conn.getMetaData();
			rs = metaData.getTables(null, schema, null, new String[] { "TABLE" });
			while (rs.next()) {
				String tableName = rs.getString("TABLE_NAME");
				result.put(tableName.toLowerCase(), tableName);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean isMySQL() {
		if (database.getProductName().toLowerCase().contains("mysql")) {
			return true;
		}
		return false;
	}

	public Table getTableInfo(String tableName) { 
		Table table = new Table();
		table.setName(tableName);

		ResultSet rs = null;
		try {
			// 获取表基本信息 
			DatabaseMetaData metaData = conn.getMetaData();
			rs = metaData.getTables(null, schema, tableName, new String[] { "TABLE" });
			if (rs.next()) {
				table.setComment(rs.getString("REMARKS"));
			}
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		table.setColumns(getTableColumns(tableName));
		table.setPrimaryKeys(getTablePrimaryKeys(tableName));
		return table;
	}

	private List<Column> getTableColumns(String tableName) {
		if(isMySQL()){
			return getTableColumnsByMySQL(tableName);
		}
		return getTableColumnsByMetadata(tableName);
	}
	private List<Column> getTableColumnsByMySQL(String tableName) {
		List<Column> columns = new ArrayList<Column>();

		
		ResultSet rs = null;
		try {
		
			String tableFieldsSql = String.format("show full fields from %s", tableName);
			  rs = conn.prepareStatement(tableFieldsSql).executeQuery();
			 
			while (rs.next()) {
				Column column = new Column();
				column.setName(rs.getString("Field"));
				column.setType(rs.getString("Type"));
				column.setSize(-1);
				column.setComment(rs.getString("Comment"));
				column.setNullable(rs.getBoolean("Null"));
				column.setDefaultValue(rs.getString("Default"));
				columns.add(column);
			}
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return columns;
	}
	private List<Column> getTableColumnsByMetadata(String tableName) {
		List<Column> columns = new ArrayList<Column>();

		ResultSet rs = null;
		try {
			// 获取字段基本信息 
			DatabaseMetaData metaData = conn.getMetaData();
			rs = metaData.getColumns(null, schema, tableName, null);
			while (rs.next()) {
				Column column = new Column();
				column.setName(rs.getString("COLUMN_NAME"));
				column.setType(rs.getString("TYPE_NAME"));
				column.setSize(rs.getInt("COLUMN_SIZE"));
				column.setComment(rs.getString("REMARKS"));
				column.setNullable(rs.getBoolean("NULLABLE"));
				column.setDefaultValue(rs.getString("COLUMN_DEF"));
				columns.add(column);
			}
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return columns;
	}

	private List<PrimaryKey> getTablePrimaryKeys(String tableName) {
		List<PrimaryKey> primaryKeys = new ArrayList<PrimaryKey>();

		ResultSet rs = null;
		try {
			// 获取字段基本信息 
			DatabaseMetaData metaData = conn.getMetaData();
			rs = metaData.getPrimaryKeys(null, schema, tableName);
			while (rs.next()) {
				PrimaryKey obj = new PrimaryKey();
				obj.setColumnName(rs.getString("COLUMN_NAME"));
				obj.setKeySeq(rs.getInt("KEY_SEQ"));
				obj.setPkName(rs.getString("PK_NAME"));
				primaryKeys.add(obj);
			}
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return primaryKeys;
	}
}

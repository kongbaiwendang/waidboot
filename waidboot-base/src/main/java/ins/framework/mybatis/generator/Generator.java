package ins.framework.mybatis.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class Generator extends BaseGenerator {
	private GenFileInfo voInfo;
	private GenFileInfo poInfo;
	private GenFileInfo daoInfo;
	private GenFileInfo baseMapperXmlInfo;
	private GenFileInfo mapperXmlInfo;

	private String assemblePackage(String catalog, String module) {
		String result = genConfig.getBasePackage() + "." + catalog;
		if (module != null && module.trim().length() > 0) {
			result = result + "." + module;
		}
		return result;
	}

	private String assembleXmlPackage(String module) {
		String result = "";
		if (module != null && module.trim().length() > 0) {
			result = module;
		} else {
			result = "misc";
		}
		return result;
	}

	private void resetFileInfo(String beanName, String module) {
		String saveDir = genConfig.getSaveDir();
		// VO
		String name = beanName + "Vo";
		String packageName = assemblePackage("vo", module);
		String path = getFilePath(genConfig.getSaveDirForVo(), getPathFromPackageName(packageName));
		voInfo = new GenFileInfo(name, packageName, path);
		// PO
		name = beanName;
		packageName = assemblePackage("po", module);
		path = getFilePath(saveDir, getPathFromPackageName(packageName));
		poInfo = new GenFileInfo(name, packageName, path);

		// Dao
		name = beanName + "Dao";
		packageName = assemblePackage("dao", module);
		path = getFilePath(saveDir, getPathFromPackageName(packageName));
		daoInfo = new GenFileInfo(name, packageName, path);

		// BaseMapperXml
		name = beanName + "BaseDao";
		packageName = assembleXmlPackage(module);
		String xmlPath = getFilePath(genConfig.getSaveDirForXml(), "base");
		path = getFilePath(xmlPath, getPathFromPackageName(packageName));
		baseMapperXmlInfo = new GenFileInfo(name, packageName, path);

		// MapperXml
		name = beanName + "Dao";
		packageName = assembleXmlPackage(module); 
		xmlPath = getFilePath(genConfig.getSaveDirForXml(), "custom");
		path = getFilePath(xmlPath, getPathFromPackageName(packageName));
		mapperXmlInfo = new GenFileInfo(name, packageName, path);
	}

	@Override
	protected void run(Table table, String module) throws Exception {
		System.out.println("============处理表" + table.getName() + "==================");
		if (table.getPrimaryKeys().size() == 0) {
			System.out.println("表" + table.getName() + "没有主键字段，忽略生成，请手工编写.");
			return;
		}
		if (table.getPrimaryKeys().size() > 1) {
			System.out.println("表" + table.getName() + "为联合主键，忽略生成,请手工编写.");
			return;
		}
		String beanName = getBeanName(table.getName(), false);
		resetFileInfo(beanName, module);

		// System.out.println(voInfo);
		// System.out.println(poInfo);
		// System.out.println(daoInfo);
		// System.out.println(baseMapperXmlInfo);
		// System.out.println(mapperXmlInfo);
		fileOvervide = false;
		if (containsGenType(GenType.VO) && validFile(voInfo.getPath(), voInfo.getName(), JAVA_SUFFIX)) {
			buildVo(table);
		}
		fileOvervide = true;
		if (containsGenType(GenType.PO) && validFile(poInfo.getPath(), poInfo.getName(), JAVA_SUFFIX)) {
			buildPo(table);
		}

		fileOvervide = false;
		if (containsGenType(GenType.DAO) && validFile(daoInfo.getPath(), daoInfo.getName(), JAVA_SUFFIX)) {
			buildDao(table);
		}
		fileOvervide = true;
		if (containsGenType(GenType.BASE_MAPPER_XML)
				&& validFile(baseMapperXmlInfo.getPath(), baseMapperXmlInfo.getName(), XML_SUFFIX)) {
			buildBaseXml(table);
		}
		fileOvervide = false;
		if (containsGenType(GenType.MAPPER_XML)
				&& validFile(mapperXmlInfo.getPath(), mapperXmlInfo.getName(), XML_SUFFIX)) {
			buildXml(table);
		}

		// List<Column> columns = table.getColumns();
		// for (int i = 0; i < columns.size(); i++) {
		// Column column = columns.get(i);
		// System.out.println("\t" + column);
		// }
		// System.out.println("\t----------------------------------");
		//
		// List<PrimaryKey> primaryKeys = table.getPrimaryKeys();
		// for (int i = 0; i < primaryKeys.size(); i++) {
		// PrimaryKey primaryKey = primaryKeys.get(i);
		// System.out.println("\t" + primaryKey);
		// }
	}

	private List<String> getTableColumnTypes(Table table) {
		List<String> types = new ArrayList<String>();
		List<Column> columns = table.getColumns();
		int size = columns.size();
		for (int i = 0; i < size; i++) {
			Column column = columns.get(i);
			types.add(column.getType());
		}
		return types;
	}

	/**
	 * 生成PO类
	 *
	 * @param table
	 *            table
	 */
	protected void buildPo(Table table) throws IOException {
		List<String> types = getTableColumnTypes(table);
		File beanFile = new File(poInfo.getPath(), poInfo.getName() + ".java");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(beanFile)));
		bw.write("package " + poInfo.getPackageName() + ";");
		bw.newLine();
		bw.newLine();
		bw.write("import java.io.Serializable;");
		bw.newLine();
		if (isDate(types)) {
			bw.write("import java.util.Date;");
			bw.newLine();
		}
		if (isDecimal(types)) {
			bw.write("import java.math.BigDecimal;");
			bw.newLine();
		}
		bw.newLine();
		bw.write("import lombok.Data;");
		bw.newLine();
		String classComment = "对应表名：" + table.getName();
		if (table.getComment() != null && table.getComment().trim().length() > 0) {
			classComment = classComment + ",备注：" + table.getComment().trim();
		}
		bw = buildClassComment(bw, "通过ins-framework-mybatis工具自动生成，请勿手工修改。表" + table.getName() + "的PO对象<br/>",
				classComment);

		bw.newLine();
		bw.write("@Data");
		bw.newLine();
		bw.write("public class " + poInfo.getName() + " implements Serializable {");
		bw.newLine();
		bw.write("\tprivate static final long serialVersionUID = 1L;");
		bw.newLine();
		List<Column> columns = table.getColumns();
		int size = columns.size();
		for (int i = 0; i < size; i++) {
			Column column = columns.get(i);
			String field = processField(column.getName());
			bw.write("\t/** 对应字段：" + column.getName());
			String comment = column.getComment();
			if (comment != null && comment.trim().length() > 0) {
				bw.write(",备注：" + comment.trim());
			}
			bw.write(" */");
			bw.newLine();
			bw.write("\tprivate " + processType(column.getType()) + " " + field + ";");
			bw.newLine();
		}

		bw.newLine();
		bw.write("}");
		bw.newLine();
		bw.flush();
		bw.close();
		System.out.println("Generate PO file " + beanFile.getAbsolutePath());
	}

	/**
	 * 生成VO类
	 *
	 * @param table
	 *            table
	 */
	protected void buildVo(Table table) throws IOException {
		List<String> types = getTableColumnTypes(table);
		File beanFile = new File(voInfo.getPath(), voInfo.getName() + ".java");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(beanFile)));
		bw.write("package " + voInfo.getPackageName() + ";");
		bw.newLine();
		bw.newLine();
		bw.write("import java.io.Serializable;");
		bw.newLine();
		if (isDate(types)) {
			bw.write("import java.util.Date;");
			bw.newLine();
		}
		if (isDecimal(types)) {
			bw.write("import java.math.BigDecimal;");
			bw.newLine();
		}
		bw.newLine();
		bw.write("import lombok.Data;");
		bw.newLine();
		String classComment = "对应表名：" + table.getName();
		if (table.getComment() != null && table.getComment().trim().length() > 0) {
			classComment = classComment + ",备注：" + table.getComment().trim();
		}
		bw = buildClassComment(bw, "通过ins-framework-mybatis工具自动生成。表" + table.getName() + "的VO对象<br/>", classComment);

		bw.newLine();
		bw.write("@Data");
		bw.newLine();
		bw.write("public class " + voInfo.getName() + " implements Serializable {");
		bw.newLine();
		bw.write("\tprivate static final long serialVersionUID = 1L;");
		bw.newLine();
		List<Column> columns = table.getColumns();
		int size = columns.size();
		for (int i = 0; i < size; i++) {
			Column column = columns.get(i);
			String field = processField(column.getName());
			bw.write("\t/** 对应字段：" + column.getName());
			String comment = column.getComment();
			if (comment != null && comment.trim().length() > 0) {
				bw.write(",备注：" + comment.trim());
			}
			bw.write(" */");
			bw.newLine();
			bw.write("\tprivate " + processType(column.getType()) + " " + field + ";");
			bw.newLine();
		}

		bw.newLine();
		bw.write("}");
		bw.newLine();
		bw.flush();
		bw.close();
		System.out.println("Generate VO file " + beanFile.getAbsolutePath());
	}

	/**
	 * 构建Mapper文件
	 *
	 * @param beanName
	 * @param mapperName
	 * @throws IOException
	 */
	protected void buildDao(Table table) throws IOException {
		List<PrimaryKey> primaryKeys = table.getPrimaryKeys();
		if (primaryKeys.size() != 1) {
			throw new IllegalArgumentException("目前只支持单一主键的表");
		}
		PrimaryKey primaryKey = primaryKeys.get(0);
		Column idColumn = null;
		List<Column> columns = table.getColumns();
		int size = columns.size();
		for (int i = 0; i < size; i++) {
			Column column = columns.get(i);
			if (column.getName().equalsIgnoreCase(primaryKey.getColumnName())) {
				idColumn = column;
				break;
			}
		}
		if (idColumn == null) {

			throw new IllegalArgumentException("找不到主键名对应的字段");
		}

		File mapperFile = new File(daoInfo.getPath(), daoInfo.getName() + ".java");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mapperFile), "utf-8"));
		bw.write("package " + daoInfo.getPackageName() + ";");
		bw.newLine();
		bw.newLine();
		bw.write("import " + poInfo.getPackageName() + "." + poInfo.getName() + ";");
		bw.newLine();
		bw.write("import ins.framework.mybatis.MybatisBaseDao;");
		bw.newLine();

		bw = buildClassComment(bw, "", "表" + table.getName() + "对应的基于MyBatis实现的Dao接口<br/>\r\n * 在其中添加自定义方法");
		bw.newLine();
		bw.write("public interface " + daoInfo.getName() + " extends MybatisBaseDao<" + poInfo.getName() + ", "
				+ processType(idColumn.getType()) + "> {");

		bw.newLine();
		bw.newLine();
		bw.write("}");
		bw.flush();
		bw.close();
		System.out.println("Generate Dao file " + mapperFile.getAbsolutePath());
	}

	/**
	 * 构建实体类映射XML文件
	 *
	 * @param table
	 *            table
	 */
	protected void buildBaseXml(Table table) throws IOException {
		List<Column> columns = table.getColumns();
		File mapperXmlFile = new File(baseMapperXmlInfo.getPath(), baseMapperXmlInfo.getName() + ".xml");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mapperXmlFile)));
		bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		bw.newLine();
		bw.write(
				"<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
		bw.newLine();
		bw.write("<!-- ============================================================== -->");
		bw.newLine();
		bw.write("<!-- ============================================================== -->");
		bw.newLine();
		bw.write("<!-- =======通过ins-framework-mybatis工具自动生成，请勿手工修改！======= -->");
		bw.newLine();
		bw.write("<!-- =======本配置文件中定义的节点可在自定义配置文件中直接使用！       ======= -->");
		bw.newLine();
		bw.write("<!-- ============================================================== -->");
		bw.newLine();
		bw.write("<!-- ============================================================== -->");
		bw.newLine();
		bw.write("<mapper namespace=\"" + daoInfo.getPackageName() + "." + daoInfo.getName() + "\">");
		bw.newLine();
		bw.write("\t<!-- 默认开启二级缓存,使用Least Recently Used（LRU，最近最少使用的）算法来收回 -->");
		bw.newLine();
		bw.write("\t<cache/>");
		bw.newLine();
		/*
		 * 下面开始写SqlMapper中的方法
		 */
		List<PrimaryKey> primaryKeys = table.getPrimaryKeys();
		PrimaryKey primaryKey = primaryKeys.get(0);

		buildBaseDaoSQL_BaseResultMap(bw, table, primaryKey, columns);
		buildBaseDaoSQL_Base_Column_List(bw, table, primaryKey, columns);
		buildBaseDaoSQL_Base_Select_By_Entity_Where(bw, table, primaryKey, columns);
		buildBaseDaoSQL_Base_Select_By_Entity(bw, table, primaryKey, columns);
		buildBaseDaoSQL_SelectByPrimaryKey(bw, table, primaryKey, columns);
		buildBaseDaoSQL_SelectBatchByPrimaryKeys(bw, table, primaryKey, columns);
//		buildBaseDaoSQL_SelectOne(bw, table, primaryKey, columns);
		buildBaseDaoSQL_SelectPage(bw, table, primaryKey, columns);
		buildBaseDaoSQL_DeleteByPrimaryKey(bw, table, primaryKey, columns);
		buildBaseDaoSQL_DeleteBatchByPrimaryKeys(bw, table, primaryKey, columns);
		buildBaseDaoSQL_Insert(bw, table, primaryKey, columns);
		buildBaseDaoSQL_InsertSelective(bw, table, primaryKey, columns);
		buildBaseDaoSQL_UpdateSelectiveByPrimaryKey(bw, table, primaryKey, columns);
		buildBaseDaoSQL_UpdateByPrimaryKey(bw, table, primaryKey, columns);

		bw.write("</mapper>");
		bw.flush();
		bw.close();
		System.out.println("Generate BaseXml file " + mapperXmlFile.getAbsolutePath());
	}

	/**
	 * 构建实体类映射XML文件
	 *
	 * @param table
	 *            table
	 */
	protected void buildXml(Table table) throws IOException {
		File mapperXmlFile = new File(mapperXmlInfo.getPath(), mapperXmlInfo.getName() + ".xml");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mapperXmlFile)));
		bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		bw.newLine();
		bw.write(
				"<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
		bw.newLine();
		bw.write("<!-- ============================================================== -->");
		bw.newLine();
		bw.write("<!-- ================可直接使用Base配置文件中定义的节点！================ -->");
		bw.newLine();
		bw.write("<!-- ============================================================== -->");
		bw.newLine();
		bw.write("<mapper namespace=\"" + daoInfo.getPackageName() + "." + daoInfo.getName() + "\">");
		bw.newLine();

		bw.write("  <!-- 请在下方添加自定义配置-->");
		bw.newLine();
		bw.newLine();
		bw.newLine();
		bw.write("</mapper>");
		bw.flush();
		bw.close();
		System.out.println("Generate Xml file " + mapperXmlFile.getAbsolutePath());
	}

	/**
	 * 通用返回参数
	 *
	 * @param bw
	 * @param idMap
	 * @param columns
	 * @throws IOException
	 */
	protected void buildBaseDaoSQL_BaseResultMap(BufferedWriter bw, Table table, PrimaryKey primaryKey,
			List<Column> columns) throws IOException {
		int size = columns.size();
		bw.write("\t<!-- 通用查询结果对象-->");
		bw.newLine();
		bw.write(
				"\t<resultMap id=\"BaseResultMap\" type=\"" + poInfo.getPackageName() + "." + poInfo.getName() + "\">");
		bw.newLine();

		for (int i = 0; i < size; i++) {
			Column column = columns.get(i);
			if (column.getName().equalsIgnoreCase(primaryKey.getColumnName())) {
				bw.write("\t\t <id ");
			} else {
				bw.write("\t\t <result ");
			}
			bw.write("column=\"" + column.getName() + "\" ");
			bw.write("property=\"" + processField(column.getName()) + "\"/> ");
			bw.newLine();
		}
		bw.write("\t</resultMap>");
		bw.newLine();
		bw.newLine();
	}

	protected void buildBaseDaoSQL_Base_Column_List(BufferedWriter bw, Table table, PrimaryKey primaryKey,
			List<Column> columns) throws IOException {
		int size = columns.size();
		bw.write("\t<!-- 通用查询结果列-->");
		bw.newLine();
		bw.write("\t<sql id=\"Base_Column_List\">");
		bw.newLine();
		bw.write("\t\t");
		for (int i = 0; i < size; i++) {
			Column column = columns.get(i);
			bw.write(" " + column.getName());
			if (column.getName().contains("_")) {
				bw.write(" AS " + processField(column.getName()));
			}
			if (i != size - 1) {
				bw.write(",");
			}
			if (i % 5 == 4) {
				bw.newLine();
				bw.write("\t\t");
			}
		}
		bw.newLine();
		bw.write("\t</sql>");
		bw.newLine();
		bw.newLine();
	}

	protected void buildBaseDaoSQL_Base_Select_By_Entity_Where(BufferedWriter bw, Table table, PrimaryKey primaryKey,
			List<Column> columns) throws IOException {
		int size = columns.size();
		bw.write("\t<!-- 按对象查询记录的WHERE部分 -->");
		bw.newLine();
		bw.write("\t<sql id=\"Base_Select_By_Entity_Where\">");
		bw.newLine();
		for (int i = 0; i < size; i++) {
			Column column = columns.get(i);
			bw.write("\t\t<if test=\"" + processField(column.getName()) + " != null\" >");
			bw.newLine();
			bw.write("\t\t\tand " + column.getName() + " = #{" + processField(column.getName()) + "}");
			bw.newLine();
			bw.write("\t\t</if>");
			bw.newLine();
		}
		bw.write("\t</sql>");
		bw.newLine();
		bw.newLine();
	}

	protected void buildBaseDaoSQL_Base_Select_By_Entity(BufferedWriter bw, Table table, PrimaryKey primaryKey,
			List<Column> columns) throws IOException {
		bw.write("\t<!-- 按对象查询记录的SQL部分 -->");
		bw.newLine();
		bw.write("\t<sql id=\"Base_Select_By_Entity\">");
		bw.newLine();
		bw.write("\t\tselect");
		bw.newLine();
		bw.write("\t\t\t<include refid=\"Base_Column_List\" />");
		bw.newLine();
		bw.write("\t\tfrom " + table.getName());
		bw.newLine();
		bw.write("\t\t<where>");
		bw.newLine();
		bw.write("\t\t\t<include refid=\"Base_Select_By_Entity_Where\" />");
		bw.newLine();
		bw.write("\t\t</where>");
		bw.newLine();
		bw.write("\t</sql>");
		bw.newLine();
		bw.newLine();
	}

	protected void buildBaseDaoSQL_SelectByPrimaryKey(BufferedWriter bw, Table table, PrimaryKey primaryKey,
			List<Column> columns) throws IOException {
		bw.write("\t<!-- 按主键查询一条记录 -->");
		bw.newLine();
		bw.write("\t<select id=\"selectByPrimaryKey\" resultMap=\"BaseResultMap\" parameterType=\"map\">");
		bw.newLine();
		bw.write("\t\tselect");
		bw.newLine();
		bw.write("\t\t\t<include refid=\"Base_Column_List\" />");
		bw.newLine();
		bw.write("\t\tfrom " + table.getName());
		bw.newLine();
		bw.write("\t\twhere " + primaryKey.getColumnName() + " = #{param1}");
		bw.newLine();
		bw.write("\t</select>");
		bw.newLine();
		bw.newLine();
	}

	protected void buildBaseDaoSQL_SelectBatchByPrimaryKeys(BufferedWriter bw, Table table, PrimaryKey primaryKey,
			List<Column> columns) throws IOException {
		bw.write("\t<!-- 按主键List查询多条记录 -->");
		bw.newLine();
		bw.write("\t<select id=\"selectBatchByPrimaryKeys\" resultMap=\"BaseResultMap\" parameterType=\"map\">");
		bw.newLine();
		bw.write("\t\tselect");
		bw.newLine();
		bw.write("\t\t\t<include refid=\"Base_Column_List\" />");
		bw.newLine();
		bw.write("\t\tfrom " + table.getName());
		bw.newLine();
		bw.write("\t\twhere " + primaryKey.getColumnName() + " in");
		bw.newLine();
		bw.write("\t\t<foreach item=\"item\" index=\"index\" collection=\"list\" open=\"(\" separator=\",\" close=\")\">");
		bw.newLine();
		bw.write("\t\t\t#{item}");
		bw.newLine();
		bw.write("\t\t</foreach>");
		bw.newLine();
		bw.write("\t</select>");
		bw.newLine();
		bw.newLine();
	}

	protected void buildBaseDaoSQL_SelectOne(BufferedWriter bw, Table table, PrimaryKey primaryKey,
			List<Column> columns) throws IOException {
		bw.write("\t<!-- 按对象查询一条记录 -->");
		bw.newLine();
		bw.write("\t<select id=\"selectOne\" resultMap=\"BaseResultMap\" parameterType=\"" + poInfo.getPackageName()
				+ "." + poInfo.getName() + "\">");
		bw.newLine();
		bw.write("\t\t<include refid=\"Base_Select_By_Entity\" />");
		bw.newLine();
		bw.write("\t</select>");
		bw.newLine();
		bw.newLine();
	}

	protected void buildBaseDaoSQL_SelectPage(BufferedWriter bw, Table table, PrimaryKey primaryKey,
			List<Column> columns) throws IOException { 
		bw.write("\t<!-- 按对象查询一页记录（多条记录） -->");
		bw.newLine();
		bw.write("\t<select id=\"selectPage\" resultMap=\"BaseResultMap\" parameterType=\"" + poInfo.getPackageName()
				+ "." + poInfo.getName() + "\">");
		bw.newLine();
		bw.write("\t\t<include refid=\"Base_Select_By_Entity\" />");
		bw.newLine();
		bw.write("\t</select>");
		bw.newLine();
		bw.newLine();
	}

	protected void buildBaseDaoSQL_DeleteByPrimaryKey(BufferedWriter bw, Table table, PrimaryKey primaryKey,
			List<Column> columns) throws IOException {
		bw.write("\t<!-- 按主键删除一条记录 -->");
		bw.newLine();
		bw.write("\t<delete id=\"deleteByPrimaryKey\" parameterType=\"map\">");
		bw.newLine();
		bw.write("\t\tdelete from " + table.getName());
		bw.newLine();
		bw.write("\t\twhere " + primaryKey.getColumnName() + " = #{param1}");
		bw.newLine();
		bw.write("\t</delete>");
		bw.newLine();
		bw.newLine();
	}

	protected void buildBaseDaoSQL_DeleteBatchByPrimaryKeys(BufferedWriter bw, Table table, PrimaryKey primaryKey,
			List<Column> columns) throws IOException {
		bw.write("\t<!-- 按主键List删除多条记录 -->");
		bw.newLine();
		bw.write("\t<delete id=\"deleteBatchByPrimaryKeys\" parameterType=\"map\">");
		bw.newLine();
		bw.write("\t\tdelete from " + table.getName());
		bw.newLine();
		bw.write("\t\twhere " + primaryKey.getColumnName() + " in "); 
		bw.newLine();
		bw.write("\t\t<foreach item=\"item\" index=\"index\" collection=\"list\" open=\"(\" separator=\",\" close=\")\">");
		bw.newLine();
		bw.write("\t\t\t#{item}");
		bw.newLine();
		bw.write("\t\t</foreach>");
		bw.newLine();
		bw.write("\t</delete>");
		bw.newLine();
		bw.newLine();
	}

	/**
	 * 通用返回参数
	 *
	 * @param bw
	 * @param idMap
	 * @param columns
	 * @throws IOException
	 */
	protected void buildBaseDaoSQL_Insert(BufferedWriter bw, Table table, PrimaryKey primaryKey, List<Column> columns)
			throws IOException {
		int size = columns.size();
		bw.write("\t<!-- 完整插入一条记录-->");
		bw.newLine();
		bw.write("\t<insert id=\"insert\" parameterType=\"" + poInfo.getPackageName() + "." + poInfo.getName() + "\">");
		bw.newLine();
		bw.write("\t\tinsert into " + table.getName() + " (");
		for (int i = 0; i < size; i++) {
			Column column = columns.get(i);
			bw.write(column.getName());
			if (i != size - 1) {
				bw.write(", ");
			}
			if (i % 5 == 4) {
				bw.newLine();
				bw.write("\t\t\t");
			}
		}
		bw.write(")");
		bw.newLine();
		bw.write("\t\tvalues(");
		for (int i = 0; i < size; i++) {
			Column column = columns.get(i);
			bw.write("#{" + processField(column.getName()));
			bw.write("}");
			if (i != size - 1) {
				bw.write(", ");
			}
			if (i % 5 == 4) {
				bw.newLine();
				bw.write("\t\t\t");
			}
		}
		bw.write(")");
		bw.newLine();
		bw.write("\t</insert>");
		bw.newLine();
		bw.newLine();
	}

	protected void buildBaseDaoSQL_InsertSelective(BufferedWriter bw, Table table, PrimaryKey primaryKey,
			List<Column> columns) throws IOException {
		int size = columns.size();
		bw.write("\t<!-- 插入一条记录(为空的字段不操作) -->");
		bw.newLine();
		bw.write("\t<insert id=\"insertSelective\" parameterType=\"" + poInfo.getPackageName() + "." + poInfo.getName()
				+ "\">");
		bw.newLine();
		bw.write("\t\tinsert into " + table.getName() + "");
		bw.newLine();
		bw.write("\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\" >");
		bw.newLine();
		for (int i = 0; i < size; i++) {
			Column column = columns.get(i);
			bw.write("\t\t\t<if test=\"" + processField(column.getName()) + " != null\" >");
			bw.newLine();
			bw.write("\t\t\t\t" + column.getName() + ",");
			bw.newLine();
			bw.write("\t\t\t</if>");
			bw.newLine();
		}
		bw.write("\t\t</trim>");
		bw.newLine();
		bw.write("\t\tvalues <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\" >");
		bw.newLine();
		for (int i = 0; i < size; i++) {
			Column column = columns.get(i);
			bw.write("\t\t\t<if test=\"" + processField(column.getName()) + " != null\" >");
			bw.newLine();
			bw.write("\t\t\t\t#{" + processField(column.getName()) + "},");
			bw.newLine();
			bw.write("\t\t\t</if>");
			bw.newLine();
		}
		bw.write("\t\t</trim>");
		bw.newLine();
		bw.write("\t</insert>");
		bw.newLine();
		bw.newLine();
	}

	protected void buildBaseDaoSQL_UpdateSelectiveByPrimaryKey(BufferedWriter bw, Table table, PrimaryKey primaryKey,
			List<Column> columns) throws IOException {
		int size = columns.size();
		bw.write("\t<!-- 更新一条记录(为空的字段不操作) -->");
		bw.newLine();
		bw.write("\t<update id=\"updateSelectiveByPrimaryKey\" parameterType=\"" + poInfo.getPackageName() + "."
				+ poInfo.getName() + "\">");
		bw.newLine();
		bw.write("\t\tupdate " + table.getName());
		bw.newLine();
		bw.write("\t\t<set>");
		bw.newLine();
		for (int i = 0; i < size; i++) {
			Column column = columns.get(i);
			if (column.getName().equalsIgnoreCase(primaryKey.getColumnName())) {
				continue;
			}
			bw.write("\t\t\t<if test=\"" + processField(column.getName()) + " != null\" >");
			bw.newLine();
			bw.write("\t\t\t\t" + column.getName() + "=#{" + processField(column.getName()) + "},");
			bw.newLine();
			bw.write("\t\t\t</if>");
			bw.newLine();
		}
		bw.write("\t\t</set>");
		bw.newLine();
		bw.write("\t\twhere " + primaryKey.getColumnName() + " = #{" + processField(primaryKey.getColumnName()) + "}");
		bw.newLine();
		bw.write("\t</update>");
		bw.newLine();
		bw.newLine();
	}

	protected void buildBaseDaoSQL_UpdateByPrimaryKey(BufferedWriter bw, Table table, PrimaryKey primaryKey,
			List<Column> columns) throws IOException {
		int size = columns.size();
		bw.write("\t<!-- 完整更新一条记录 -->");
		bw.newLine();
		bw.write("\t<update id=\"updateByPrimaryKey\" parameterType=\"" + poInfo.getPackageName() + "."
				+ poInfo.getName() + "\">");
		bw.newLine();
		bw.write("\t\tupdate " + table.getName());
		bw.newLine();
		bw.write("\t\tset ");
		for (int i = 0; i < size; i++) {
			Column column = columns.get(i);
			if (column.getName().equalsIgnoreCase(primaryKey.getColumnName())) {
				continue;
			}
			bw.write(column.getName() + "=#{" + processField(column.getName()) + "}");
			if (i != size - 1) {
				bw.write(",");
				bw.newLine();

				bw.write("\t\t\t");
			}
		}
		bw.newLine();
		bw.write("\t\twhere " + primaryKey.getColumnName() + " = #{" + processField(primaryKey.getColumnName()) + "}");
		bw.newLine();
		bw.write("\t</update>");
		bw.newLine();
		bw.newLine();
	}
}

package ins.framework.mybatis.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 映射文件自动生成类
 * </p>
 * 
 */
abstract class BaseGenerator {

	protected static final String JAVA_SUFFIX = ".java";
	protected static final String XML_SUFFIX = ".xml";

	protected GenConfig genConfig;
	protected List<GenParam> paramList;
	protected Database database;

	protected boolean fileOvervide = false;

	/**
	 * run 执行
	 */
	protected abstract void run(Table table, String basePackage) throws Exception;

	public void setGenConfig(GenConfig genConfig) {
		this.genConfig = genConfig;
	}

	public void setParamList(List<GenParam> paramList) {
		this.paramList = paramList;
	}

	/**
	 * 根据包名转换成具体路径
	 *
	 * @param packageName
	 * @return
	 */
	protected static String getPathFromPackageName(String packageName) {
		if (StringUtils.isEmpty(packageName)) {
			return "";
		}
		return packageName.replace(".", File.separator);
	}

	/**
	 * 生成文件地址
	 *
	 * @param segment
	 *            文件地址片段
	 * @return
	 */
	protected static String getFilePath(String savePath, String segment) {
		File folder = new File(savePath, segment);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		return folder.getPath();
	}

	protected boolean containsGenType(GenType genType) {
		for (GenType gen : genConfig.getGenTypes()) {
			if (gen==(genType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 生成映射文件
	 */
	public void generate() {
		//设置SaveDirForVo默认值
		if (genConfig.getSaveDirForVo() == null
				|| genConfig.getSaveDirForVo().trim().length() == 0) {
			genConfig.setSaveDirForVo(genConfig.getSaveDir());
		}
		//设置SaveDirForXml默认值
		if (genConfig.getSaveDirForXml() == null
				|| genConfig.getSaveDirForXml().trim().length() == 0) {
			genConfig.setSaveDirForXml(new File(genConfig.getSaveDir(), "../resources/mapper").getAbsolutePath());
		}
		if (containsGenType(GenType.VO) && genConfig.getSaveDirForVo() == null
				|| genConfig.getSaveDirForVo().trim().length() == 0) {
			throw new IllegalArgumentException("生成VO时需要设置SaveDirForVo参数");
		}
		if (containsGenType(GenType.MAPPER_XML) && genConfig.getSaveDirForXml() == null
				|| genConfig.getSaveDirForXml().trim().length() == 0) {
			throw new IllegalArgumentException("生成Mapper XML时需要设置SaveDirForXml参数");
		}
		if (containsGenType(GenType.BASE_MAPPER_XML) && genConfig.getSaveDirForXml() == null
				|| genConfig.getSaveDirForXml().trim().length() == 0) {
			throw new IllegalArgumentException("生成Base Mapper XML时需要设置SaveDirForXml参数");
		}
		
		try {
			Class.forName(genConfig.getDbDriverName());
			Properties props = new Properties();
			props.setProperty("user", genConfig.getDbUser());
			props.setProperty("password", genConfig.getDbPassword());
			props.setProperty("remarks", "true"); // 设置可以获取remarks信息
			props.setProperty("useInformationSchema", "true");// 设置可以获取tables
																// remarks信息

			Connection conn = DriverManager.getConnection(genConfig.getDbUrl(), props);

			DatabaseUtils databaseUtils = DatabaseUtils.getInstance(conn, genConfig.getDbSchema());
			database = databaseUtils.database;
			/**
			 * 根据配置获取应该生成文件的表信息
			 */
			Map<String, String> tableNamesMap = databaseUtils.getAllTableNamesMap();
			if (tableNamesMap.size() == 0) {
				return;
			}

			for (GenParam genParam : paramList) {
				String[] tableNames = genParam.getTables();
				for (int i = 0; i < tableNames.length; i++) {
					String tableName = tableNames[i].toLowerCase();
					if (!tableNamesMap.containsKey(tableName)) {
						System.err.println("Can't find table " + tableName);
						continue;
					}
					Table table = databaseUtils.getTableInfo(tableNamesMap.get(tableName));
					run(table, genParam.getModule());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	/**
	 * 根据是否覆盖标志决定是否覆盖当前已存在的文件
	 *
	 * @param dirPath
	 * @param beanName
	 * @param suffix
	 * @return
	 */
	protected boolean validFile(String dirPath, String beanName, String suffix) {
		File file = new File(dirPath, beanName + suffix);
		return !file.exists() || fileOvervide;
	}

	/**
	 * 生成 beanName
	 *
	 * @param table
	 *            表名
	 * @return beanName
	 */
	protected String getBeanName(String table, boolean includePrefix) {
		StringBuilder sb = new StringBuilder();
		if (table.contains("_")) {
			String[] tables = table.split("_");
			int l = tables.length;
			int s = 0;
			if (includePrefix) {
				s = 1;
			}
			for (int i = s; i < l; i++) {
				String temp = tables[i].trim();
				sb.append(temp.substring(0, 1).toUpperCase()).append(temp.substring(1).toLowerCase());
			}
		} else {
			sb.append(table.substring(0, 1).toUpperCase()).append(table.substring(1).toLowerCase());
		}
		return sb.toString();
	}

	protected String processType(String type) {
		if (database.getProductName().toLowerCase().contains("oracle")) {
			return oracleProcessType(type);
		}
		return mysqlProcessType(type);
	}

	/**
	 * MYSQL字段类型转换
	 *
	 * @param type
	 *            字段类型
	 * @return
	 */
	protected String mysqlProcessType(String type) {
		String t = type.toLowerCase();
		if (t.contains("char")) {
			return "String";
		} else if (t.contains("bigint")) {
			return "Long";
		} else if (t.contains("int")) {
			return "Integer";
		} else if (t.contains("date") || t.contains("timestamp")) {
			return "Date";
		} else if (t.contains("text")) {
			return "String";
		} else if (t.contains("binary(1)")) {
			return "Boolean";
		} else if (t.contains("bit")) {
			return "Boolean";
		} else if (t.contains("decimal")) {
			return "BigDecimal";
		} else if (t.contains("blob")) {
			return "byte[]";
		} else if (t.contains("float")) {
			return "Float";
		} else if (t.contains("double")) {
			return "Double";
		}
		System.err.println("unkown type [" + type + "]");
		return null;
	}

	/**
	 * ORACLE字段类型转换
	 *
	 * @param type
	 *            字段类型
	 * @return
	 */
	protected String oracleProcessType(String type) {
		String t = type.toUpperCase();
		if (t.contains("CHAR")) {
			return "String";
		} else if (t.contains("DATE") || t.contains("TIMESTAMP")) {
			return "Date";
		} else if (t.contains("NUMBER")) {
			return "Double";
		} else if (t.contains("FLOAT")) {
			return "Float";
		} else if (t.contains("BLOB")) {
			return "Object";
		} else if (t.contains("RAW")) {
			return "byte[]";
		}
		System.err.println("unkown type [" + type + "]");
		return null;
	}

	/**
	 * 字段是否为日期类型
	 *
	 * @param types
	 *            字段类型列表
	 * @return
	 */
	protected boolean isDate(List<String> types) {
		for (String type : types) {
			String t = type.toLowerCase();
			if (t.contains("date") || t.contains("timestamp")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 字段是否为浮点数类型
	 *
	 * @param types
	 *            字段类型列表
	 * @return
	 */
	protected boolean isDecimal(List<String> types) {
		for (String type : types) {
			if (type.toLowerCase().contains("decimal")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 字段处理
	 * 
	 * @param field
	 *            表字段
	 * @return
	 */
	protected String processField(String field) {
		/*
		 * 处理下划线分割命名字段
		 */
		StringBuilder sb = new StringBuilder();
		String[] fields = field.split("_");
		sb.append(fields[0].toLowerCase());
		for (int i = 1; i < fields.length; i++) {
			String temp = fields[i];
			sb.append(temp.substring(0, 1).toUpperCase());
			sb.append(temp.substring(1).toLowerCase());
		}
		return sb.toString();
	}

	/**
	 * 构建类上面的注释
	 *
	 * @param bw
	 * @param text
	 * @return
	 * @throws IOException
	 */
	protected BufferedWriter buildClassComment(BufferedWriter bw, String prefix, String text) throws IOException {
		bw.newLine();
		bw.write("/**");
		bw.newLine();
		bw.write(" *");
		bw.newLine();
		if (prefix != null && prefix.trim().length() > 0) {
			bw.write(" * " + prefix);
			bw.newLine();
		}
		bw.write(" * " + text);
		bw.newLine();
		bw.write(" *");
		bw.newLine();
		bw.write(" */");
		return bw;
	}

	protected void openDir() {
		/**
		 * 自动打开生成文件的目录
		 * <p>
		 * 根据 osName 执行相应命令
		 * </p>
		 */
		try {
			String osName = System.getProperty("os.name");
			if (osName != null) {
				if (osName.contains("Mac")) {
					Runtime.getRuntime().exec("open " + genConfig.getSaveDir());
				} else if (osName.contains("Windows")) {
					Runtime.getRuntime().exec("cmd /c start " + genConfig.getSaveDir());
				} else {
					System.err.println("save dir:" + genConfig.getSaveDir());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

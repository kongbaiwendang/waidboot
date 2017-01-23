package ins.framework.mybatis.generator;

import lombok.Data;

/**
 * <p>
 * 生成器配置类
 * 
 * <pre> 
 * saveDir 文件生成目录  
 * tableNames   	要生成的表名称，如为空就直接指定所有表.格式为逗号分割
 * fileOverride 	是否覆盖当前已有文件
 * -------------------------------------
 * 以下数据库相关配置：
 * -------------------------------------
 * db_include_prefix 表是否包含前缀，例如: tb_xxx 其中 tb_ 为前缀
 * db_driverName 驱动
 * db_user 用户名
 * db_password 密码
 * db_url 连接地址 
 * </pre>
 * </p>
 */
@Data
public class GenConfig {

	private String basePackage;
	protected String saveDir;
	protected String saveDirForVo;
	protected String saveDirForXml;
	private GenType[] genTypes;

	/*
	 * 指定生成表名
	 */
	protected String[] tableNames = null;

	/*
	 * 是否覆盖当前路径下已有文件（默认 true）
	 */
	protected boolean fileOverride = true;

	/* db_config */
	protected boolean dbPrefix = false;

	/*
	 * 数据库字段使用下划线命名（默认 false）
	 */
	protected boolean dbColumnUnderline = false;

	protected String dbDriverName;

	protected String dbUser;

	protected String dbPassword;

	protected String dbUrl;
	protected String dbSchema; 

}

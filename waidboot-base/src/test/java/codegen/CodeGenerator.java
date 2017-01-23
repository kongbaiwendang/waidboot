package codegen;

import java.util.ArrayList;
import java.util.List;

import ins.framework.mybatis.generator.GenConfig;
import ins.framework.mybatis.generator.GenParam;
import ins.framework.mybatis.generator.GenType;
import ins.framework.mybatis.generator.Generator;

public class CodeGenerator {

	public static void main(String[] args) {
		List<GenParam> paramList = new ArrayList<GenParam>();
		paramList.add(new GenParam("addressinfo", new String[] { "addressinfo" }));
		GenConfig gc = new GenConfig();
		gc.setBasePackage("com.waidboot");
		// mysql 数据库相关配置
		// 设置基本保存目录（Java源代码根目录）
		// gc.setSaveDir("D:/Work/lab_cloud/server/misc/misc-server/src/main/java");
		gc.setSaveDir("src/main/java");
		//VO为独立项目时可使用setSaveDirForVo来设置不同的目录
		//gc.setSaveDirForVo(new File(gc.getSaveDir(), "../../../../misc-vo/src/main/java").getAbsolutePath());
		gc.setDbDriverName("com.mysql.jdbc.Driver");
		gc.setDbUser("root");
		gc.setDbSchema("myframework1");
		gc.setDbPassword("wydwyd");
		gc.setDbUrl("jdbc:mysql://localhost:3306/myframework1?useUnicode=true&characterEncoding=utf-8&useSSL=false");
		// 生成PO\VO（自动覆盖）、BaseMapperXML（覆盖）、Dao（不覆盖）、MapperXML（不覆盖）
		// 支持生成的文件类型
		gc.setGenTypes(
				new GenType[] { GenType.VO, GenType.PO, GenType.DAO, GenType.BASE_MAPPER_XML, GenType.MAPPER_XML });
		Generator generator = new Generator();
		generator.setGenConfig(gc);
		generator.setParamList(paramList);
		generator.generate();
	}
}

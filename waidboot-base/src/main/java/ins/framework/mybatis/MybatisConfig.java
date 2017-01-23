package ins.framework.mybatis;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import com.github.miemiedev.mybatis.paginator.dialect.DB2Dialect;
import com.github.miemiedev.mybatis.paginator.dialect.H2Dialect;
import com.github.miemiedev.mybatis.paginator.dialect.HSQLDialect;
import com.github.miemiedev.mybatis.paginator.dialect.MySQLDialect;
import com.github.miemiedev.mybatis.paginator.dialect.OracleDialect;
import com.github.miemiedev.mybatis.paginator.dialect.PostgreSQLDialect;
import com.github.miemiedev.mybatis.paginator.dialect.SQLServerDialect;
import com.github.miemiedev.mybatis.paginator.dialect.SybaseDialect;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableTransactionManagement
@Slf4j
public class MybatisConfig implements TransactionManagementConfigurer {
	@Autowired
	private DataSource dataSource;

	@Bean
	public OffsetLimitInterceptor offsetLimitInterceptor() throws SQLException {
		DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
		String databaseProductName = metaData.getDatabaseProductName();
		OffsetLimitInterceptor offsetLimitInterceptor = new OffsetLimitInterceptor();
		if (databaseProductName.toLowerCase().contains("mysql")) {
			offsetLimitInterceptor.setDialectClass(MySQLDialect.class.getName());
		} else if (databaseProductName.toLowerCase().contains("oracle")) {
			offsetLimitInterceptor.setDialectClass(OracleDialect.class.getName());
		} else if (databaseProductName.toLowerCase().contains("db2")) {
			offsetLimitInterceptor.setDialectClass(DB2Dialect.class.getName());
		} else if (databaseProductName.toLowerCase().contains("postgre")) {
			offsetLimitInterceptor.setDialectClass(PostgreSQLDialect.class.getName());
		} else if (databaseProductName.toLowerCase().contains("sql server")) {
			offsetLimitInterceptor.setDialectClass(SQLServerDialect.class.getName());
		} else if (databaseProductName.toLowerCase().contains("h2")) {
			offsetLimitInterceptor.setDialectClass(H2Dialect.class.getName());
		} else if (databaseProductName.toLowerCase().contains("hsql")) {
			offsetLimitInterceptor.setDialectClass(HSQLDialect.class.getName());
		} else if (databaseProductName.toLowerCase().contains("sybase")) {
			offsetLimitInterceptor.setDialectClass(SybaseDialect.class.getName());
		} else {
			throw new IllegalArgumentException("Unsupport Database [" + databaseProductName + "]");
		}
		log.info("Current databaseProductName is [" + databaseProductName + "]");
		return offsetLimitInterceptor;
	}

	@Bean
	@Override
	public PlatformTransactionManager annotationDrivenTransactionManager() {
		return new DataSourceTransactionManager(dataSource);
	}
	
}

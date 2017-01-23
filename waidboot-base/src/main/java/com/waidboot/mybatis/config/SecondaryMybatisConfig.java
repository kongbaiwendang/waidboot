/**
 * 作者：王亚冬
 * 时间：2017-01-23
 * 说明：
 *
 */
package com.waidboot.mybatis.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * 作者：王亚冬 时间：2017-01-23 说明：
 */
@Configuration
@ConfigurationProperties(prefix = "mybatis")
@MapperScan(basePackages="com.waidboot.dao.secondsource",sqlSessionFactoryRef="sqlSessionFactory_1") // 扫面下面的mapper类
public class SecondaryMybatisConfig {
	
	private Resource[] mapperLocations;
	
	/**
	 * @return the mapperLocations
	 */
	public Resource[] getMapperLocations() {
		return mapperLocations;
	}

	/**
	 * @param mapperLocations the mapperLocations to set
	 */
	public void setMapperLocations(Resource[] mapperLocations) {
		this.mapperLocations = mapperLocations;
	}

	@Autowired
	@Qualifier("secondDataSource")
	DataSource secondDataSource;

	@Bean("sqlSessionFactory_1")
	public SqlSessionFactory sqlSessionFactory() throws Exception {
		SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
		sessionFactory.setDataSource(secondDataSource);
		sessionFactory.setMapperLocations(mapperLocations);
		return sessionFactory.getObject();
	}
}

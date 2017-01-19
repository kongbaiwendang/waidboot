package com.wyd.wydframwork;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

@ImportResource({"classpath*:spring/*.xml"})
@ComponentScan("ins,com")
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
	
//	@Bean
//	DatabaseDao databaseDao(){
//		return new DatabaseDaoHibernateImpl();
//	}
	
	
}

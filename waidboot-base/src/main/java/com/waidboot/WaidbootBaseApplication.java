package com.waidboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@ComponentScan("com") //扫描下面的包@service @controller 等  
@SpringBootApplication
public class WaidbootBaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(WaidbootBaseApplication.class, args);
	}
}

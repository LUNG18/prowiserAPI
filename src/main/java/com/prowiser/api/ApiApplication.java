package com.prowiser.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RequestMapping;

@SpringBootApplication
@MapperScan("com.prowiser.api.mapper")
@EnableScheduling
@EnableTransactionManagement //配置事务
@Controller
public class ApiApplication extends SpringBootServletInitializer {
	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(ApiApplication.class);
	}


	@RequestMapping("")
	public String page(String page){
		return page;
	}
}

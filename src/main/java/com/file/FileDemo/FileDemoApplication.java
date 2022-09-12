package com.file.FileDemo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
public class FileDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileDemoApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(ApplicationContext context) {
		return args -> {

			String[] beanNames = context.getBeanDefinitionNames();
//			out.println( "Display Name : " + ctx.getDisplayName());
//			out.println("Application name: " + ctx.getApplicationName());
//			out.println("Id :"  + ctx.getId());
//			out.println(ctx.getAutowireCapableBeanFactory());
//			out.println(Arrays.toString(ctx.getAliases("")));

			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
//				System.out.println(beanName);
			}
		};
	}
}

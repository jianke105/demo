package com.boge.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * boot start
 */
@EnableWebMvc
@EnableAutoConfiguration
@SpringBootConfiguration
@SpringBootApplication(scanBasePackages = "com.boge.demo")
@MapperScan("com.boge.demo.mapper")
@EnableTransactionManagement
public class App {
    public static void main(String[] args) {
        System.out.println("boot success!");
        SpringApplication.run(App.class, args);

    }
}

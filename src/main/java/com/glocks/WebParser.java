/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.glocks;

import com.glocks.parser.MainController;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.context.annotation.ComponentScan;


@EnableAutoConfiguration
@EnableCaching
@EnableEncryptableProperties
@EnableJpaAuditing
@SpringBootApplication(scanBasePackages = {"com.glocks"})
@ComponentScan(basePackages = { "com.glocks" })
    
public class WebParser {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(WebParser.class, args);
        MainController mainController  = context.getBean(MainController.class);
        mainController.loader(context);
        context = null;

    }
}

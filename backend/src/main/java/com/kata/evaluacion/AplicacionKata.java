package com.kata.evaluacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AplicacionKata {

    public static void main(String[] args) {
        SpringApplication.run(AplicacionKata.class, args);
    }
}

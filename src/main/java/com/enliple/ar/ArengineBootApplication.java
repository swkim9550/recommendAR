package com.enliple.ar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ArengineBootApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplicationBuilder()
                .sources(ArengineBootApplication.class)
                .listeners(new ApplicationPidFileWriter("/home/users/rpapp/home/pid/arBoot.pid"))
                .build();
        application.run(args);
    }
}

package org.reddlice.chatwith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ChatWithApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatWithApplication.class, args);
    }

}

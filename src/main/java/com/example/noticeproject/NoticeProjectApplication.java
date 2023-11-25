package com.example.noticeproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NoticeProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoticeProjectApplication.class, args);
    }

}

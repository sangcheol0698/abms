package kr.co.abacus.abms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class AbmsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AbmsApiApplication.class, args);
    }

}

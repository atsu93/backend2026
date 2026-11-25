package app;

import app.service.StreamingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommandLineRunner initData(StreamingService streamingService) {
        return args -> {
            streamingService.createMedia("SpongeBob SquarePants", "G", false);
            streamingService.createMedia("Stranger Things", "PG-13", false);
            streamingService.createMedia("The Witcher", "R", true);
        };
    }
}

package hello;

/**
 * Created by seanwilliams on 3/20/17.
 *
 * Starts the Spring Boot web application.
 */
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
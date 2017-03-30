package hello;

import com.google.edgemicro.annotations.EdgeMicro;
import com.google.edgemicro.annotations.EdgeMicroPrivateConfig;
import com.google.edgemicro.annotations.OAuth;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by seanwilliams on 3/20/17.
 */

@RestController
@Configuration
@ComponentScan(value = {"com.google.edgemicro", "com.google.edgemicro.annotations"})
@EdgeMicro(privateConfig = true, org = "demo", env = "prod", admin = "username", password = "password", port = 8001)
@EdgeMicroPrivateConfig(runtimeURL = "http://domainorip:9001", mgmtURL = "http://domainorip:8080")
@OAuth(allowNoAuthorization = true, enabled = false)
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return new Greeting(counter.incrementAndGet(),
                String.format(template, name));
    }
}
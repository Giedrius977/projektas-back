package lt.ca.javau12.furnibay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "lt.ca.javau12.furnibay")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}

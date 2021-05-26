package mfi.photos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "classpath:application.yaml")
public class PhotosServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(PhotosServerApplication.class, args);
	}
}

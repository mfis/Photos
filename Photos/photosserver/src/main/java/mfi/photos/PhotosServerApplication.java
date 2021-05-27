package mfi.photos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@ComponentScan(basePackages = {"mfi.files", "mfi.photos"})
@PropertySource(value = "classpath:application.yaml")
@PropertySource(value = "file:/Users/mfi/documents/config/photosapp.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:/home/photosapp/documents/config/photosapp.properties", ignoreResourceNotFound = true)
public class PhotosServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(PhotosServerApplication.class, args);
	}
}

package id.sevenspeed.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

@SpringBootApplication
public class TrackingApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(TrackingApplication.class);

	public static void main(String[] args) {
		loadDotEnv();
		SpringApplication.run(TrackingApplication.class, args);
	}

	private static void loadDotEnv() {
		try (Stream<String> lines = Files.lines(Paths.get(".env"))) {
			lines
					.filter(line -> !line.startsWith("#") && line.contains("="))
					.forEach(line -> {
						String[] parts = line.split("=", 2);
						if (parts.length == 2) {
							String key = parts[0].trim();
							String value = parts[1].trim();
							System.setProperty(key, value);
						}
					});
		} catch (IOException e) {
			LOGGER.error("Could not load .env file: {}", e.getMessage());
		}
	}
}

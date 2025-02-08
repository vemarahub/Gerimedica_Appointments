package nl.gerimedica.assignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Spring Boot application class for Patient Manager System.
 * This class serves as the entry point for the application and handles the initial bootstrap.
 * Extends SpringBootServletInitializer to support deployment to external servlet containers.
 *
 * @author rnair
 * @version 1.0
 */
@SpringBootApplication
public class PatientManagerApplication {

	/**
	 * Logger instance for this class.
	 */
	private static final Logger logger = LoggerFactory.getLogger(PatientManagerApplication.class);

	public static void main(String[] args) {

		try {
			SpringApplication app = new SpringApplication(PatientManagerApplication.class);
			Environment env = app.run(args).getEnvironment();

			logger.info("""
                Application '{}' is running! Access URLs:
                Local: \thttp://localhost:{}
                """,
					env.getProperty("spring.application.name"),
					env.getProperty("server.port")
			);
		} catch (Exception e) {
			logger.error("Error starting application", e);
			throw e;
		}

	}

}

package cat.copernic.proyecto2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import cat.copernic.proyecto2.DAO.Mongo.TraduccionDAO;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "cat.copernic.proyecto2.DAO", excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = TraduccionDAO.class))
@EnableMongoRepositories(basePackages = "cat.copernic.proyecto2.DAO", includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = TraduccionDAO.class))
public class Proyecto2Application {

	public static void main(String[] args) {
		SpringApplication.run(Proyecto2Application.class, args);
	}

}

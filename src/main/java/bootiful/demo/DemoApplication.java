package bootiful.demo;

//import bootiful.demo.config.AppSpecificHints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@ImportRuntimeHints(AppSpecificHints.class)
@SpringBootApplication
public class DemoApplication {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

//	@Bean
//	ApplicationRunner cities(CityDao cityDao) {
//		return args -> {
//			var newCity = new City(null, "NYC", "NY", "USA");
//			cityDao.insert(newCity);
//			log.info("New city: {}", newCity);
//			cityDao.findAll().forEach(x -> log.info("{}", x));
//		};
//	}


}



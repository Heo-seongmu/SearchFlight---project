package hsm.bootproject.SearchFlight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class SearchFlightApplication {

	public static void main(String[] args) {
		SpringApplication.run(SearchFlightApplication.class, args);
	}

}

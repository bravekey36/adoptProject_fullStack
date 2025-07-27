package petcare;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("petcare.mapper")
public class PetcareStsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetcareStsApplication.class, args);
	}

}

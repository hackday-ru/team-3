package net.b2ok;

import com.google.gson.Gson;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChatieApplication {


	public static Gson gson = new Gson();

	public static void main(String[] args) {
		SpringApplication.run(ChatieApplication.class, args);
	}
}

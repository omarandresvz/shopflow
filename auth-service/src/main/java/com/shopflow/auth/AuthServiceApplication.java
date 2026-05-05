package com.shopflow.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.shopflow.security.config.JwtFilterConfig;
import com.shopflow.security.config.JwtSecurityConfig;

@SpringBootApplication
@Import({
        JwtSecurityConfig.class,
        JwtFilterConfig.class
})
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

}

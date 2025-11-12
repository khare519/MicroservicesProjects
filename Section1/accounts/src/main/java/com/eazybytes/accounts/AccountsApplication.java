package com.eazybytes.accounts;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef="auditorAwareImpl")
@OpenAPIDefinition(
		info = @Info(
				title = "Accounts microservices REST API Documentation",
				description = "Bank System Accounts microservices REST API Documentation",
				version = "v1",
				contact = @Contact(
						name  ="Satyam Khare",
                        email = "satyam123@gmail.com",
						url ="https://www.google.com"
				),
				license = @License(
						name="Apache 2.0",
						url="https://www.google.com"
				)
		),
		externalDocs = @ExternalDocumentation(
				description = "Accounts Microservices",
				url = "https://www.google.com"
		)
)
public class AccountsApplication {


	public static void main(String[] args) {
		SpringApplication.run(AccountsApplication.class, args);
	}

}

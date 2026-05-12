package br.com.scheiner.aws.console;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class AwsConsoleApplication {

	public static void main(String[] args) {
		SpringApplication.run(AwsConsoleApplication.class, args);
	}

}

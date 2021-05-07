package com.tisza.tarock;

import org.apache.log4j.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.web.servlet.support.*;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;

@SpringBootApplication
@Configuration
@EnableScheduling
public class Main extends SpringBootServletInitializer
{
	private static final Logger log = Logger.getLogger(Main.class);

	public static void main(String[] args)
	{
		SpringApplication.run(Main.class, args);
	}
}

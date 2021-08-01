package com.tisza.tarock;

import org.apache.log4j.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.web.servlet.support.*;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.web.filter.*;

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

	@Configuration
	public class RequestLoggingFilterConfig {

		@Bean
		public CommonsRequestLoggingFilter logFilter() {
			CommonsRequestLoggingFilter filter
					= new CommonsRequestLoggingFilter();
			filter.setIncludeQueryString(true);
			filter.setIncludePayload(true);
			filter.setMaxPayloadLength(10000);
			filter.setIncludeHeaders(false);
			filter.setAfterMessagePrefix("REQUEST DATA : ");
			return filter;
		}
	}
}

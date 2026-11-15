package com.freightfox.alumni_searcher.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

	@Value("${phantombuster.api-key}")
	private String apiKey;

	@Bean
	public RestClient restClient() {

		return RestClient.builder().baseUrl("https://api.phantombuster.com/api/v2")
				.defaultHeader("X-Phantombuster-Key", apiKey).build();
	}
}
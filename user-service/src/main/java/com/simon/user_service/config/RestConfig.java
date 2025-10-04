package com.simon.user_service.config;

import com.simon.utils.HttpRequest;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public HttpRequest httpRequest(RestTemplate restTemplate) {
        return new HttpRequest(restTemplate);
    }
}

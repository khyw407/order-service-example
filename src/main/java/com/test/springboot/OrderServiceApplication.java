package com.test.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.springboot.domain.Order;
import com.test.springboot.repository.OrderRepository;
import com.test.springboot.service.OrderService;

@SpringBootApplication
@EnableEurekaClient
@EnableCaching
@EnableHystrix
@EnableBinding(Processor.class)
public class OrderServiceApplication {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceApplication.class);

	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired
	OrderService orderService;
	
	@StreamListener(Processor.INPUT)
	public void receiveOrder(Order order) throws JsonProcessingException{
		LOGGER.info("Order received: {}", objectMapper.writeValueAsString(order));
		orderService.process(order);
	}
	
	@LoadBalanced
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}
	
	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager("customers");
	}
	
	@Bean
	OrderRepository repository() {
		return new OrderRepository();
	}
	
	@Bean
	public CommonsRequestLoggingFilter requestLoggingFilter() {
	    CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
	    loggingFilter.setIncludePayload(true);
	    loggingFilter.setIncludeHeaders(true);
	    loggingFilter.setMaxPayloadLength(1000);
	    loggingFilter.setAfterMessagePrefix("REQ:");
	    return loggingFilter;
	}
}

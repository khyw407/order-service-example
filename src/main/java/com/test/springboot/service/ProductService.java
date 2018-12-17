package com.test.springboot.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.test.springboot.domain.Product;

@Service
public class ProductService {

	@Autowired
	CacheManager cacheManager;
	
	@Autowired
	RestTemplate restTemplate;
	
	@HystrixCommand(commandKey = "product-service.findByIds", commandProperties = {
		@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000"),
		@HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
		@HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "30"),
		@HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),
		@HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "10000")
	})
	public List<Product> findProductsByIds(List<Long> ids) {
		Product[] products = restTemplate.postForObject("zuul.192.168.0.9.nip.io:32001/api/product/ids", ids, Product[].class);
		return Arrays.stream(products).collect(Collectors.toList());
	}
}

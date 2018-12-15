package com.test.springboot.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.test.springboot.domain.Customer;

@FeignClient(name = "customer-service")
public interface CustomerClient {
	@GetMapping("/withAccounts/{customerId}")
	Customer findByIdWithAccounts(@PathVariable("customerId") Long customerId);
}

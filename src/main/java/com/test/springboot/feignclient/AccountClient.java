package com.test.springboot.feignclient;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import com.test.springboot.domain.Account;

@FeignClient(name = "account-service")
public interface AccountClient {
	@PutMapping("/withdraw/{accountId}/{amount}")
	Account withdraw(@PathVariable("accountId") Long id, @PathVariable("amount") int amount);
}

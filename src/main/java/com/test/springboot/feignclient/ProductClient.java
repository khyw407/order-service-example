package com.test.springboot.feignclient;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import com.test.springboot.domain.Product;

@FeignClient(name = "product-service")
public interface ProductClient {
	@PostMapping("/ids")
	List<Product> findByIds(List<Long> ids);
}

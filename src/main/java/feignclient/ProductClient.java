package feignclient;

import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import domain.Product;

@FeignClient(name = "product-service")
public interface ProductClient {
	@PostMapping("/ids")
	List<Product> findByIds(List<Long> ids);
}

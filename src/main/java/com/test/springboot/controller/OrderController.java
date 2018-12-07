package com.test.springboot.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.test.springboot.domain.Account;
import com.test.springboot.domain.Customer;
import com.test.springboot.domain.Order;
import com.test.springboot.domain.OrderStatus;
import com.test.springboot.domain.Product;
import com.test.springboot.feignclient.AccountClient;
import com.test.springboot.feignclient.CustomerClient;
import com.test.springboot.feignclient.ProductClient;
import com.test.springboot.repository.OrderRepository;

@RestController
public class OrderController {
	
	@Autowired
	OrderRepository repository;
	
	@Autowired
	AccountClient accountClient;
	
	@Autowired
	CustomerClient customerClient;
	
	@Autowired
	ProductClient productClient;
	
	@PostMapping
	public Order prepare(@RequestBody Order order) {
		int price = 0;
		
		List<Product> products = productClient.findByIds(order.getProductIds());
		Customer customer = customerClient.findByIdWithAccounts(order.getCustomerId());
		
		for(Product product : products) {
			price += product.getPrice();
		}
		
		int discountedPrice = priceDiscount(price, customer);
		
		Optional<Account> account = customer.getAccounts().stream()
									.filter(a -> (a.getBalance() > discountedPrice)).findFirst();
		
		if(account.isPresent()) {
			order.setAccountId(account.get().getId());
			order.setStatus(OrderStatus.ACCEPTED);
			order.setPrice(discountedPrice);
		}else {
			order.setStatus(OrderStatus.REJECTED);
		}
		
		return repository.add(order);
	}
	
	@PutMapping("/{id}")
	public Order accept(@PathVariable Long id) throws Exception{
		Order order = repository.findById(id);
		
		accountClient.withdraw(order.getAccountId(), order.getPrice());
		order.setStatus(OrderStatus.DONE);
		repository.update(order);
		
		return order;
	}
	
	public int priceDiscount(int price, Customer customer) {
		double discount = 0;
		
		switch(customer.getType()) {
		case REGULAR:
			discount = 0.05;
			break;
		case VIP:
			discount = 0.1;
			break;
		default:
			break;
		}
		
		return (int)(price - (price * discount));
	}
}

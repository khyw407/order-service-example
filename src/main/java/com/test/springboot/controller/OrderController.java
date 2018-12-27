package com.test.springboot.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.springboot.domain.Account;
import com.test.springboot.domain.Customer;
import com.test.springboot.domain.Order;
import com.test.springboot.domain.OrderStatus;
import com.test.springboot.domain.Product;
import com.test.springboot.repository.OrderRepository;
import com.test.springboot.service.AccountService;
import com.test.springboot.service.CustomerService;
import com.test.springboot.service.OrderSender;
import com.test.springboot.service.ProductService;

@RestController
public class OrderController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Autowired
	OrderRepository repository;
	
	@Autowired
	AccountService accountService;
	
	@Autowired
	CustomerService customerService;
	
	@Autowired
	ProductService productService;
	
	@Autowired
	OrderSender orderSender;
	
	@PostMapping("/prepare")
	public Order prepare(@RequestBody Order order) throws JsonProcessingException{
		int price = 0;
		
		List<Product> products = productService.findProductsByIds(order.getProductIds());
		Customer customer = customerService.findCustomerWithAccounts(order.getCustomerId());
		
		LOGGER.info("Products found: {}", mapper.writeValueAsString(products));
		LOGGER.info("Customer found: {}", mapper.writeValueAsString(customer));
		
		for(Product product : products) {
			price += product.getPrice();
		}
		
		int discountedPrice = priceDiscount(price, customer);
		LOGGER.info("Discounted price: {}", mapper.writeValueAsString(Collections.singletonMap("price", discountedPrice)));
		
		Optional<Account> account = customer.getAccounts().stream()
									.filter(a -> (a.getBalance() > discountedPrice)).findFirst();
		
		if(account.isPresent()) {
			order.setAccountId(account.get().getId());
			order.setStatus(OrderStatus.ACCEPTED);
			order.setPrice(discountedPrice);
			
			LOGGER.info("Account found: {}", mapper.writeValueAsString(account.get()));
		}else {
			order.setStatus(OrderStatus.REJECTED);
			
			LOGGER.info("Account not found: {}", mapper.writeValueAsString(customer.getAccounts()));
		}
		
		return repository.add(order);
	}
	
	@PutMapping("/{id}")
	public Order accept(@PathVariable Long id) throws JsonProcessingException{
		Order order = repository.findById(id);
		LOGGER.info("Order found: {}", mapper.writeValueAsString(order));
		
		accountService.withdraw(order.getAccountId(), order.getPrice());
		
		HashMap<String, Object> log = new HashMap<>();
		log.put("accountId", order.getAccountId());
		log.put("price", order.getPrice());
		LOGGER.info("Account modified: {}", mapper.writeValueAsString(log));
		
		order.setStatus(OrderStatus.DONE);
		LOGGER.info("Order status changed: {}", mapper.writeValueAsString(Collections.singletonMap("status", order.getStatus())));
		
		repository.update(order);
		
		return order;
	}
	
	public int priceDiscount(int price, Customer customer) {
		double discount = 0;
		int ordersNum = repository.countByCustomerId(customer.getId());
		
		if (ordersNum > 10) {
			ordersNum = 10;
		}
		
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
		
		discount += (ordersNum * 0.01);
		
		return (int)(price - (price * discount));
	}
	
	@PostMapping
	public Order process(@RequestBody Order order) throws JsonProcessingException{
		Order rtnOrder = repository.add(order);
		LOGGER.info("Order saved: {}", mapper.writeValueAsString(order));
		
		boolean isSent = orderSender.send(rtnOrder);
		LOGGER.info("Order sent: {}", mapper.writeValueAsString(Collections.singletonMap("isSent", isSent)));
		
		return rtnOrder;
	}
}

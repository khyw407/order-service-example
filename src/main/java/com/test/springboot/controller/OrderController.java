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
import com.test.springboot.repository.OrderRepository;
import com.test.springboot.service.AccountService;
import com.test.springboot.service.CustomerService;
import com.test.springboot.service.ProductService;

@RestController
public class OrderController {
	
	@Autowired
	OrderRepository repository;
	
	@Autowired
	AccountService accountService;
	
	@Autowired
	CustomerService customerService;
	
	@Autowired
	ProductService productService;
	
	@PostMapping
	public Order prepare(@RequestBody Order order) {
		int price = 0;
		
		List<Product> products = productService.findProductsByIds(order.getProductIds());
		Customer customer = customerService.findCustomerWithAccounts(order.getCustomerId());
		
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
	public Order accept(@PathVariable Long id){
		Order order = repository.findById(id);
		
		accountService.withdraw(order.getAccountId(), order.getPrice());
		order.setStatus(OrderStatus.DONE);
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
}

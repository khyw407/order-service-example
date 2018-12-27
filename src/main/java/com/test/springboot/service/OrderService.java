package com.test.springboot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.springboot.domain.Order;
import com.test.springboot.domain.OrderStatus;
import com.test.springboot.repository.OrderRepository;

@Service
public class OrderService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);
	
	private ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	OrderRepository orderRepository;
	
	public void process(final Order order) throws JsonProcessingException{
		LOGGER.info("Order processed: {}", objectMapper.writeValueAsString(order));
		
		Order o = orderRepository.findById(order.getId());
		
		if(o.getStatus() != OrderStatus.REJECTED) {
			o.setStatus(order.getStatus());
			orderRepository.update(o);
			
			LOGGER.info("Order status updated: {}", objectMapper.writeValueAsString(order));
		}
	}
}

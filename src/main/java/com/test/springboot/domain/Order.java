package com.test.springboot.domain;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter @Setter
public class Order {
	private Long id;
	private OrderStatus status;
	private int price;
	private Long customerId;
	private Long accountId;
	private List<Long> productIds;
}

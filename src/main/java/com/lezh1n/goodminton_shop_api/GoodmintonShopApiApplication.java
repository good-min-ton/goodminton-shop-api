package com.lezh1n.goodminton_shop_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class GoodmintonShopApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoodmintonShopApiApplication.class, args);
	}

}

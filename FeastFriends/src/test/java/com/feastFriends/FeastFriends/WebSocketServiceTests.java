package com.feastFriends.FeastFriends;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.feastFriends.feastFriends.service.RedisService;
import com.feastFriends.feastFriends.service.RestaurantService;
import com.feastFriends.feastFriends.service.UserService;

import jakarta.annotation.PostConstruct;

public class WebSocketServiceTests {

	@Autowired
	RestaurantService restaurantService = new RestaurantService();

	@Autowired
	private UserService userService;

	private RedisService redisService;

	@Value("${redis.host}")
	private String redisHost;

	@Value("${redis.port}")
	private int redisPort;

	@PostConstruct
	public void init() {
		try {
			redisService = new RedisService(redisHost, redisPort);
			// Further initialization if needed
		} catch (Exception e) {
			// Handle exception, log the error, or take corrective actions
			e.printStackTrace();
			System.err.println("Failed to initialize RedisService: " + e.getMessage());
			// You might want to rethrow the exception or handle it based on your needs
		}
	}

	@Test
	void getRequestedGenresForGroupTest() {

	}
}

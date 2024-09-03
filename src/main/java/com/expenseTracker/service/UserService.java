package com.expenseTracker.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.expenseTracker.model.User;
import com.expenseTracker.repository.UserRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public User createUser(String email, String password, String firstName, String lastName) {

		String hashedPassword = hashPassword(password);
		User newUser = new User(email, hashedPassword, firstName, lastName, LocalDateTime.now());
		return userRepository.save(newUser);
	}

	private String hashPassword(String password) {

		return passwordEncoder.encode(password);
	}

	public String getEmailByUserId(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
		return user.getEmail();
	}

}

package com.expenseTracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.expenseTracker.auth.JwtUtil;
import com.expenseTracker.model.ErrorResponse;
import com.expenseTracker.model.LoginRequest;
import com.expenseTracker.model.LoginResponse;
import com.expenseTracker.model.User;
import com.expenseTracker.service.UserService;

@Controller
@RequestMapping("/api/users")
public class UserController {
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserService userService;

	@PostMapping("/add")
	public ResponseEntity<User> addUser(@RequestParam String email, @RequestParam String password,
			@RequestParam String firstName, @RequestParam String lastName) {

		User newUser = userService.createUser(email, password, firstName, lastName);
		return ResponseEntity.ok(newUser);
	}
	
	@ResponseBody
	@PostMapping("/login")
	public ResponseEntity<Object> login(@RequestBody LoginRequest loginReq) {

		try {
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(loginReq.getEmail(), loginReq.getPassword()));
			String email = authentication.getName();
			User user = new User(email, "");
			String token = jwtUtil.createToken(user);
			LoginResponse loginRes = new LoginResponse(email, token);

			return ResponseEntity.ok(loginRes);

		} catch (BadCredentialsException e) {
			ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "Invalid username or password");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		} catch (Exception e) {
			ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

	}

}

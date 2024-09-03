package com.expenseTracker.auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final ObjectMapper mapper;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		Map<String, Object> errorDetails = new HashMap<>();

		try {
			String accessToken = jwtUtil.resolveToken(request);
			if (accessToken == null) {
				System.out.println("No JWT token found in request");
				filterChain.doFilter(request, response);
				return;
			}
			System.out.println("token : " + accessToken);
			Claims claims = jwtUtil.resolveClaims(request);

			if (claims != null & jwtUtil.validateClaims(claims)) {
				String email = claims.getSubject();
				System.out.println("email : " + email);
				Authentication authentication = new UsernamePasswordAuthenticationToken(email, "", new ArrayList<>());
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} else {
				
				System.out.println("Invalid token or claims. Rejecting request.");
				response.setStatus(HttpStatus.FORBIDDEN.value());
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				errorDetails.put("message", "Invalid or expired token");
				mapper.writeValue(response.getWriter(), errorDetails);
				return;
			}

		} catch (Exception e) {
			System.out.println("Exception in JWT processing: " + e.getMessage());
			errorDetails.put("message", "Authentication Error");
			errorDetails.put("details", e.getMessage());
			response.setStatus(HttpStatus.FORBIDDEN.value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);

			mapper.writeValue(response.getWriter(), errorDetails);

		}
		filterChain.doFilter(request, response);
	}

}

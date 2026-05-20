package com.pricemonitor.controller;

import com.pricemonitor.dto.LoginRequest;
import com.pricemonitor.dto.LoginResponse;
import com.pricemonitor.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:19006"})
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
		try {
			authService.register(request.getEmail(), request.getPassword(), request.getName(), request.getZipCode());

			LoginResponse loginResponse = authService.login(request.getEmail(), request.getPassword());

			Map<String, Object> response = new HashMap<>();
			response.put("userId", loginResponse.getUserId());
			response.put("token", loginResponse.getToken());
			response.put("email", loginResponse.getEmail());

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
		try {
			LoginResponse response = authService.login(request.getEmail(), request.getPassword());

			Map<String, Object> result = new HashMap<>();
			result.put("userId", response.getUserId());
			result.put("token", response.getToken());
			result.put("email", response.getEmail());

			return ResponseEntity.ok(result);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@PostMapping("/validate")
	public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
		try {
			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				return ResponseEntity.badRequest().body(Map.of("error", "Invalid token format"));
			}

			String token = authHeader.substring(7);
			authService.getUserFromToken(token);

			return ResponseEntity.ok(Map.of("valid", true));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("valid", false, "error", e.getMessage()));
		}
	}

	public static class RegisterRequest {
		private String email;
		private String password;
		private String name;
		private String zipCode;

		public String getEmail() { return email; }
		public void setEmail(String email) { this.email = email; }

		public String getPassword() { return password; }
		public void setPassword(String password) { this.password = password; }

		public String getName() { return name; }
		public void setName(String name) { this.name = name; }

		public String getZipCode() { return zipCode; }
		public void setZipCode(String zipCode) { this.zipCode = zipCode; }
	}
}

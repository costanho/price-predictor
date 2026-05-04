package com.pricemonitor.service;

import com.pricemonitor.dto.LoginRequest;
import com.pricemonitor.dto.LoginResponse;
import com.pricemonitor.model.Users;
import com.pricemonitor.repository.UsersRepository;
import com.pricemonitor.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UsersRepository usersRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	public void register(String email, String password, String name, String zipCode) {
		if (usersRepository.findByEmail(email).isPresent()) {
			throw new RuntimeException("Email already registered");
		}

		Users user = new Users();
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(password));
		user.setName(name);
		user.setZipCode(zipCode);
		user.setBlsRegion(determineBLSRegion(zipCode));
		user.setIsActive(true);
		user.setCreatedAt(LocalDateTime.now());
		user.setUpdatedAt(LocalDateTime.now());
		user.setSearchRadiusMiles(10);

		usersRepository.save(user);
	}

	public LoginResponse login(String email, String password) {
		Users user = usersRepository.findByEmail(email)
			.orElseThrow(() -> new RuntimeException("User not found"));

		if (!passwordEncoder.matches(password, user.getPasswordHash())) {
			throw new RuntimeException("Invalid password");
		}

		if (!user.getIsActive()) {
			throw new RuntimeException("User account is inactive");
		}

		user.setLastLoginAt(LocalDateTime.now());
		usersRepository.save(user);

		String token = jwtUtil.generateToken(user.getEmail());
		return new LoginResponse(token, user.getId(), user.getEmail());
	}

	public Users getUserFromToken(String token) {
		String email = jwtUtil.getEmailFromToken(token);
		return usersRepository.findByEmail(email)
			.orElseThrow(() -> new RuntimeException("User not found"));
	}

	private String determineBLSRegion(String zipCode) {
		if (zipCode == null || zipCode.isEmpty()) {
			return "national";
		}
		// Simple mapping based on first digit of zip code
		char firstChar = zipCode.charAt(0);
		return switch (firstChar) {
			case '0' -> "Northeast";
			case '1' -> "Northeast";
			case '2' -> "Southeast";
			case '3' -> "Southeast";
			case '4' -> "Midwest";
			case '5' -> "Midwest";
			case '6' -> "South";
			case '7' -> "South";
			case '8' -> "West";
			case '9' -> "West";
			default -> "national";
		};
	}
}

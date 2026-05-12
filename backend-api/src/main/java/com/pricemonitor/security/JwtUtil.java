package com.pricemonitor.security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.expiration}")
	private long jwtExpirationMs;

	public String generateToken(String email) {
		return Jwts.builder()
			.setSubject(email)
			.setIssuedAt(new Date())
			.setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
			.signWith(SignatureAlgorithm.HS512, jwtSecret)
			.compact();
	}

	public String generateTokenWithUserId(String userId, String email) {
		return Jwts.builder()
			.setSubject(email)
			.claim("userId", userId)
			.setIssuedAt(new Date())
			.setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
			.signWith(SignatureAlgorithm.HS512, jwtSecret)
			.compact();
	}

	public String getEmailFromToken(String token) {
		try {
			return (String) Jwts.parser()
				.setSigningKey(jwtSecret)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
		} catch (JwtException | IllegalArgumentException e) {
			throw new RuntimeException("Invalid token: " + e.getMessage());
		}
	}

	public String getUserIdFromToken(String token) {
		try {
			return (String) Jwts.parser()
				.setSigningKey(jwtSecret)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.get("userId");
		} catch (JwtException | IllegalArgumentException e) {
			throw new RuntimeException("Invalid token: " + e.getMessage());
		}
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser()
				.setSigningKey(jwtSecret)
				.build()
				.parseClaimsJws(token);
			return true;
		} catch (SecurityException e) {
			throw new RuntimeException("Invalid JWT signature: " + e.getMessage());
		} catch (MalformedJwtException e) {
			throw new RuntimeException("Invalid JWT token: " + e.getMessage());
		} catch (ExpiredJwtException e) {
			throw new RuntimeException("Expired JWT token: " + e.getMessage());
		} catch (UnsupportedJwtException e) {
			throw new RuntimeException("Unsupported JWT token: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("JWT claims string is empty: " + e.getMessage());
		}
	}

	public boolean validateTokenQuietly(String token) {
		try {
			Jwts.parser()
				.setSigningKey(jwtSecret)
				.build()
				.parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}

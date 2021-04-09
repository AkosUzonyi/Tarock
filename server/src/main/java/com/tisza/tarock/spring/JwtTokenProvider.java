package com.tisza.tarock.spring;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import javax.annotation.*;
import java.util.*;

@Component
public class JwtTokenProvider
{
	//@Value("${security.jwt.token.secret-key:secret}")
	private String secretKey = "secret"; //TODO: key
	//@Value("${security.jwt.token.expire-length:3600000}")
	private long validityInMilliseconds = 10000; // TODO: time

	@PostConstruct
	protected void init()
	{
		secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
	}

	public String createToken(int userId)
	{
		Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
		Date now = new Date();
		Date validity = new Date(now.getTime() + validityInMilliseconds);
		return Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(now)
				.setExpiration(validity)
				.signWith(SignatureAlgorithm.HS256, secretKey)
				.compact();
	}

	public int getUserId(String token)
	{
		return Integer.parseInt(Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject());
	}

	public boolean validateToken(String token)
	{
		Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
		return claims.getBody().getExpiration().after(new Date());
	}
}

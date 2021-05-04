package com.tisza.tarock.spring;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import javax.annotation.*;
import java.util.*;

@Component
public class JwtTokenProvider
{
	private byte[] secretKey;
	private long validityInMilliseconds = 3600 * 1000;

	@PostConstruct
	protected void init()
	{
		secretKey = new byte[128];
		new Random().nextBytes(secretKey);
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

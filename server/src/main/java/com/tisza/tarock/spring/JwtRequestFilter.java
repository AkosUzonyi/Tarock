package com.tisza.tarock.spring;

import org.springframework.beans.factory.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.*;
import org.springframework.stereotype.*;
import org.springframework.web.filter.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

@Component
public class JwtRequestFilter extends OncePerRequestFilter
{
	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	private void doAuthentication(HttpServletRequest request)
	{
		String requestTokenHeader = request.getHeader("Authorization");
		if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer "))
			return;

		String jwtToken = requestTokenHeader.substring(7);
		if (!jwtTokenProvider.validateToken(jwtToken))
			return;

		int userId = jwtTokenProvider.getUserId(jwtToken);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException
	{
		try
		{
			doAuthentication(request);
		}
		catch (Exception e)
		{
			logger.debug("unsuccessful authorization", e);
		}

		chain.doFilter(request, response);
	}
}

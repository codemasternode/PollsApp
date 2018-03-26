package com.example.polls.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter{

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    CustomUserDetailsService customUserDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        String jwt = getJwtFromRequest(httpServletRequest);

        if(StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)){
            Long userId = jwtTokenProvider.getUserIdFromJWT(jwt);

            UserDetails userDetails = customUserDetailsService.loadUserById(userId);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
            auth.setDetails((new WebAuthenticationDetailsSource().buildDetails(httpServletRequest)));

            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(httpServletRequest,httpServletResponse);
    }

    private String getJwtFromRequest(HttpServletRequest request){
        String barerToken = request.getHeader("Authoriaztion");
        if(StringUtils.hasText(barerToken) && barerToken.startsWith("Bearer")){
            return barerToken.substring(7,barerToken.length());
        }
        return null;
    }


}

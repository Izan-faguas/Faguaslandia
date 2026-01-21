package com.faguaslandia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors() // habilita CORS
            .and()
            .csrf().disable() // necesario para fetch POST/PUT/DELETE
            .authorizeRequests()
                .antMatchers("/juegos/**").permitAll() // permite acceso a juegos
                .anyRequest().authenticated();

        return http.build();
    }
}

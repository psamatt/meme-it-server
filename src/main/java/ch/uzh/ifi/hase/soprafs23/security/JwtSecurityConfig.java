package ch.uzh.ifi.hase.soprafs23.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;

@Configuration
@EnableWebSecurity
public class JwtSecurityConfig {

    @Bean
    public JwtRequestFilter jwtFilter(@Qualifier("userRepository") UserRepository userRepository) {
        return new JwtRequestFilter(userRepository);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            final AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain configure(final HttpSecurity httpSecurity, final JwtRequestFilter jwtFilter)
            throws Exception {

        return httpSecurity.cors().and()
                .csrf().disable()
                .authorizeHttpRequests()
                .antMatchers("/users").permitAll()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers(HttpMethod.GET, "/lobbies").permitAll()
                .anyRequest().authenticated().and()
                .headers().frameOptions().sameOrigin() // allow H2 console to be embedded in iframe
                .and()
                .sessionManagement().sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS)
                .and().addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
package com.example.racketbackend.config;

import com.example.racketbackend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtFilter
    ) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> {
                })

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                /*
                 * Return understandable messages for
                 * authentication and authorization errors.
                 */
                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint(
                                (request, response, error) -> {
                                    response.setStatus(401);
                                    response.setContentType(
                                            "text/plain"
                                    );
                                    response.getWriter().write(
                                            "Authentication required or token expired"
                                    );
                                }
                        )

                        .accessDeniedHandler(
                                (request, response, error) -> {
                                    response.setStatus(403);
                                    response.setContentType(
                                            "text/plain"
                                    );
                                    response.getWriter().write(
                                            "Access denied. ADMIN role required"
                                    );
                                }
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        /*
                         * Public authentication
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/register",
                                "/login"
                        )
                        .permitAll()

                        /*
                         * Stripe webhook does not use JWT.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/stripe/webhook"
                        )
                        .permitAll()

                        /*
                         * Everyone can view rackets and images.
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/rackets/**",
                                "/images/**",
                                "/branches/**",
                                "/dna/rackets/**"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/dna/recommendations"
                        )
                        .permitAll()

                        /*
                         * Only administrators can add,
                         * update and delete rackets.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/rackets",
                                "/images/upload"
                        )
                        .hasAuthority("ROLE_ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/rackets/**"
                        )
                        .hasAuthority("ROLE_ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/rackets/**"
                        )
                        .hasAuthority("ROLE_ADMIN")

                        /*
                         * Reviews
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/reviews/can-review/**"
                        )
                        .authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/reviews"
                        )
                        .authenticated()

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/reviews/**"
                        )
                        .authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/reviews/**"
                        )
                        .permitAll()

                        /*
                         * Wishlist
                         */
                        .requestMatchers(
                                "/wishlists/**"
                        )
                        .authenticated()

                        /*
                         * Customer payment endpoints
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/payments/stripe/**",
                                "/payments/demo/**"
                        )
                        .authenticated()

                        /*
                         * Admin payment management
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/payments",
                                "/payments/**"
                        )
                        .hasAuthority("ROLE_ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/payments/**"
                        )
                        .hasAuthority("ROLE_ADMIN")

                        /*
                         * Admin user management
                         */
                        .requestMatchers(
                                "/users/**"
                        )
                        .hasAuthority("ROLE_ADMIN")

                        /*
                         * Admin order viewing
                         */
                        .requestMatchers(
                                HttpMethod.GET,
                                "/orders"
                        )
                        .hasAuthority("ROLE_ADMIN")

                        .requestMatchers(
                                "/admin/**"
                        )
                        .hasAuthority("ROLE_ADMIN")

                        /*
                         * All remaining endpoints require login.
                         */
                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource
    corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:5173"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}
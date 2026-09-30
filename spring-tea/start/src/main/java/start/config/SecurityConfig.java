package start.config;

import com.branch.service.LoginUserService;
import framework.security.LoginUserDetails;
import com.branch.properties.JwtProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import start.filter.AuthenticationRequestFilter;
import start.filter.UserRefreshRequestFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Security 配置类
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    /**
     * 配置 SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtProperties jwtProperties,
                                           StringRedisTemplate stringRedisTemplate,
                                           LoginUserService loginUserService) throws Exception {
        UserRefreshRequestFilter userRefreshRequestFilter =
                new UserRefreshRequestFilter(jwtProperties, stringRedisTemplate,loginUserService);
        AuthenticationRequestFilter authenticationRequestFilter = new AuthenticationRequestFilter();

        http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(this::configureCors)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/user/register", "/user/login", "/user/logout").permitAll()
                        .requestMatchers("/login/code", "/login/byEmail").permitAll()
                        .requestMatchers("/img/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                // user 刷新过滤器
                .addFilterBefore(userRefreshRequestFilter, UsernamePasswordAuthenticationFilter.class)
                // 认证拦截过滤器
                .addFilterBefore(authenticationRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void configureCors(CorsConfigurer<HttpSecurity> cors) {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        cors.configurationSource(source);
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response,
                                  AuthenticationException authException) throws IOException, ServletException {
                log.warn("未认证请求: {}, 异常: {}", request.getRequestURI(), authException.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                Map<String, Object> result = new HashMap<>();
                result.put("code", 401);
                result.put("message", "请先登录");
                ObjectMapper mapper = new ObjectMapper();
                response.getWriter().write(mapper.writeValueAsString(result));
            }
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            log.warn("无权限请求: {}, 异常: {}", request.getRequestURI(), accessDeniedException.getMessage());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            Map<String, Object> result = new HashMap<>();
            result.put("code", 403);
            result.put("message", "无权限访问");
            ObjectMapper mapper = new ObjectMapper();
            response.getWriter().write(mapper.writeValueAsString(result));
        };
    }
}

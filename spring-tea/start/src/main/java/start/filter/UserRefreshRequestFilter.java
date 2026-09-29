package start.filter;

import com.branch.properties.JwtProperties;
import com.branch.util.JwtUtil;
import common.constant.JwtConstant;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static common.constant.RedisPrefixConstant.WEIBO_AUTHHEADER;


/**
 * user 用户 Token 刷新与验证过滤器
 */
@Slf4j
public class UserRefreshRequestFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public UserRefreshRequestFilter(JwtProperties jwtProperties, StringRedisTemplate stringRedisTemplate) {
        this.jwtProperties = jwtProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = extractToken(request);

        if (token == null) {
            // 没有 token，放行，由后面的认证过滤器决定是否 401
            filterChain.doFilter(request, response);
            return;
        }
        try {
            Map<String, Object> claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);

            if (claims == null) {
                filterChain.doFilter(request, response);
                return;
            }

            Long userId = Long.parseLong(claims.get(JwtConstant.USER_ID).toString());
            String standardToken = stringRedisTemplate.opsForValue().get(WEIBO_AUTHHEADER + userId);

            if (!token.equals(standardToken)) {
                log.debug("user Token 验证失败，可能已注销或被篡改, 用户ID: {}", userId);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                   null,
                    token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 滑动过期
            stringRedisTemplate.expire(WEIBO_AUTHHEADER + userId,
                    jwtProperties.getTtlMillis(), TimeUnit.SECONDS);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.debug("user JWT 处理失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}

package rw.parkshare.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;

/**
 * Runs {@link OwnershipPolicy} for every authenticated API call.
 */
@Configuration
public class OwnershipInterceptor implements HandlerInterceptor, WebMvcConfigurer {

    private final OwnershipPolicy policy;

    public OwnershipInterceptor(OwnershipPolicy policy) {
        this.policy = policy;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).addPathPatterns("/api/**");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        if (!(SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken)) {
            return true; // public endpoint; SecurityConfig already decided
        }
        Object attribute = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        Map<String, String> vars = attribute instanceof Map<?, ?> map ? (Map<String, String>) map : Map.of();
        policy.check(request, vars);
        return true;
    }
}

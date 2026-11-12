package mercadito.mercadito_llanero.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AccesoWebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                    throws Exception {
                response.setHeader("Cache-Control", "no-store");
                HttpSession sesion = request.getSession(false);
                if (sesion == null || sesion.getAttribute("usuarioId") == null) {
                    response.sendRedirect("/login.html?motivo=sesion");
                    return false;
                }
                return true;
            }
        }).addPathPatterns("/panel.html");
    }
}

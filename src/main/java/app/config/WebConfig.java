package app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Đường dẫn API mà bạn muốn cho phép
                .allowedOrigins("http://localhost:3000") // Địa chỉ frontend
                .allowedMethods("GET", "POST", "PUT", "DELETE") // Các phương thức cho phép
                .allowedHeaders("*"); // Các header cho phép
    }
}
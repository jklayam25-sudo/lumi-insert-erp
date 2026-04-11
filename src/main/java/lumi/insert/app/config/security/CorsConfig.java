package lumi.insert.app.config.security; 
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
  
public class CorsConfig implements WebMvcConfigurer{

    // @Override 
    // public void addCorsMappings(CorsRegistry registry) {
    //      registry.addMapping("/api/**") 
    //                     .allowedOrigins("http://localhost:5173") 
    //                     .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    //                     .allowedHeaders("*")
    //                     .allowCredentials(true); 
    // }
}

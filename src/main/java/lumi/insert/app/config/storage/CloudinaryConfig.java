package lumi.insert.app.config.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

/**
 * Cloudinary as 3rd storage configurations.
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.setup.url}")
    private String url;
    
    /**
     * Override Cloudinary base bean
     * @return Cloudinary bean with setup url
     */
    @Bean
    Cloudinary cloudinary(){ 
        return new Cloudinary(url);
    }
}

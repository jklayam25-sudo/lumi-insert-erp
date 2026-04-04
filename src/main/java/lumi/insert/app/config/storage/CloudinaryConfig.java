package lumi.insert.app.config.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.setup.url}")
    private String url;
    
    @Bean
    Cloudinary cloudinary(){ 
        return new Cloudinary(url);
    }
}

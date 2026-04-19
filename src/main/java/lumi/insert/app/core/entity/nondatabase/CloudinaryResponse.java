package lumi.insert.app.core.entity.nondatabase;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper class for cloudinary response. 
 * <p>Represent Url, publicId,  etc.<br> 
 * Class mapped by {@code Jackson} and can be implemented manually by{@code NoArgsConstructor, AllArgsConstructor and Builder}.</p>
 * 
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudinaryResponse {
    @JsonProperty("public_id")
    private String publicId;
    
    @JsonProperty("secure_url")
    private String secureUrl;
    
    @JsonProperty("format")
    private String format;
}
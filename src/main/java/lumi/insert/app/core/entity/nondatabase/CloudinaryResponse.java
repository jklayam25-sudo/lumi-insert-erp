package lumi.insert.app.core.entity.nondatabase;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
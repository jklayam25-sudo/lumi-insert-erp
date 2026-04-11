package lumi.insert.app.controller.wrapper;

import org.slf4j.MDC;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record WebResponse<T>(
    @Schema(description = "The actual data payload")
    T data, 
    
    @Schema(description = "Error message if any", nullable = true)
    String errors,

    @Schema(description = "Correlation ID of each request")
    String requestId
) {
    public static <T> WebResponse<T> getWrapper(T data, String errors){
        return new WebResponse<T>(data, errors, MDC.get("requestId"));
    }
}

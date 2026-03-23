package lumi.insert.app.controller;
  
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping; 
import org.springframework.web.bind.annotation.PostMapping; 
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
 
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid; 
import lumi.insert.app.controller.wrapper.WebResponse;
import lumi.insert.app.dto.request.AuthTokenCreateRequest;
import lumi.insert.app.dto.response.AuthTokenResponse;
import lumi.insert.app.service.AuthTokenService;

@RestController
@Tag(name = "Authentication", description = "Endpoints for managing user sessions and JWT tokens") 
public class AuthTokenController {
    
    @Autowired
    AuthTokenService authTokenService;

    @Operation(summary = "Login to the system", description = "Authenticates user and returns access token in body and refresh token in HttpOnly cookie")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @PostMapping(
        path = "/api/auth/login",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public ResponseEntity<WebResponse<AuthTokenResponse>> loginAuthAPI(@Valid @RequestBody AuthTokenCreateRequest request){
        AuthTokenResponse resultFromService = authTokenService.createAuthToken(request);

        WebResponse<AuthTokenResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", resultFromService.refreshToken())
        .httpOnly(true)
        .secure(false)
        .maxAge(604800)
        .path("/")
        .build(); 
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(wrappedResult);
    }

    @Operation(summary = "Refresh access token", description = "Uses the refresh token from cookie to generate a new short-lived access token")
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully")
    @PostMapping(
        path = "/api/auth/refresh",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponse<AuthTokenResponse>> refreshAuthAPI(@CookieValue(name = "refreshToken", required = true) String refreshToken){
        AuthTokenResponse resultFromService = authTokenService.refreshAuthToken(refreshToken);

        WebResponse<AuthTokenResponse> wrappedResult = WebResponse.getWrapper(resultFromService, null); 
        
        return ResponseEntity.ok(wrappedResult);
    }

    @Operation(summary = "Logout", description = "Invalidates the refresh token and clears the authentication cookie")
    @ApiResponse(responseCode = "204", description = "Logout successful")
    @DeleteMapping(
        path = "/api/auth/logout"
    )
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteAuthAPI(@CookieValue(name = "refreshToken", required = true) String refreshToken, HttpServletResponse response){
        authTokenService.deleteRefreshToken(refreshToken);
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addHeader("X-ACCESS-TOKEN", null);
        response.addCookie(cookie);
    }
}
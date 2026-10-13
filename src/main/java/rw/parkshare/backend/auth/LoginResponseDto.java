package rw.parkshare.backend.auth;

import java.util.List;

public class LoginResponseDto {

    private String token;
    private String tokenType;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;

    public LoginResponseDto() {
    }

    public LoginResponseDto(
            String token,
            String tokenType,
            Long userId,
            String email,
            String firstName,
            String lastName,
            List<String> roles
    ) {
        this.token = token;
        this.tokenType = tokenType;
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
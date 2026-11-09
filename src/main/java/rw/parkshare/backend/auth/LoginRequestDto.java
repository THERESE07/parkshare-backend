package rw.parkshare.backend.auth;

import jakarta.validation.constraints.*;

public class LoginRequestDto {

    @NotBlank
    @Size(max = 255)
    private String email;
    @NotBlank
    @Size(max = 128)
    private String password;

    public LoginRequestDto() {
    }

    public LoginRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
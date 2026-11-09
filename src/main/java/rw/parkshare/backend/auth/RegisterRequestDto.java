package rw.parkshare.backend.auth;

import jakarta.validation.constraints.*;

public class RegisterRequestDto {

    @NotBlank
    @Size(max = 100)
    private String firstName;
    @NotBlank
    @Size(max = 100)
    private String lastName;
    @NotBlank
    @Email
    @Size(max = 255)
    private String email;
    @NotBlank
    @Size(max = 30)
    private String phone;
    @NotBlank
    @Size(min = 8, max = 128)
    private String password;

    public RegisterRequestDto() {
    }

    public RegisterRequestDto(
            String firstName,
            String lastName,
            String email,
            String phone,
            String password
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.password = password;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /** DRIVER (default) or OPERATOR. Operators' lots still need admin approval. */
    @Pattern(regexp = "DRIVER|OPERATOR", message = "must be one of DRIVER, OPERATOR")
    private String accountType;

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}

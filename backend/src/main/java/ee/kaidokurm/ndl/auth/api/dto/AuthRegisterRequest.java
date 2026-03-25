package ee.kaidokurm.ndl.auth.api.dto;

public class AuthRegisterRequest {
    private String email;
    private String password;

    public AuthRegisterRequest() {
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

package ee.kaidokurm.ndl.auth.api.dto;

public class PasswordResetRequest {
    private String email;

    public PasswordResetRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

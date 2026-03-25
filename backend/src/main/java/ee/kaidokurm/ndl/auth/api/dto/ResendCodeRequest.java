package ee.kaidokurm.ndl.auth.api.dto;

public class ResendCodeRequest {
    private String email;

    public ResendCodeRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

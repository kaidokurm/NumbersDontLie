package ee.kaidokurm.ndl.auth.user;

import ee.kaidokurm.ndl.auth.user.model.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountDeletionService {

    private final UserRepository userRepository;

    public AccountDeletionService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void deleteAccountAndData(UUID userId) {
        // Deleting user cascades user-linked data via FK ON DELETE CASCADE.
        userRepository.deleteById(userId);
    }
}

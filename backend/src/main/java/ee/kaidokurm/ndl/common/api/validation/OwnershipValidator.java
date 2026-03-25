package ee.kaidokurm.ndl.common.api.validation;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Utility for ownership validation in API endpoints. Ensures that users can
 * only access/modify their own resources.
 */
public class OwnershipValidator {

    /**
     * Validates that the given user ID matches the resource owner ID. Throws 403
     * Forbidden if ownership check fails.
     * 
     * @param resourceOwnerId  the ID of the resource owner
     * @param requestingUserId the ID of the user making the request
     * @param resourceName     the name of the resource being accessed (for error
     *                         message)
     * @throws ResponseStatusException with 403 Forbidden if ownership check fails
     */
    public static void validateOwnership(java.util.UUID resourceOwnerId, java.util.UUID requestingUserId,
            String resourceName) {
        if (!resourceOwnerId.equals(requestingUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have permission to access this " + resourceName);
        }
    }

    /**
     * Validates that a resource exists and belongs to the requesting user. Throws
     * 404 Not Found if resource doesn't exist or doesn't belong to user.
     * 
     * @param resourceExists whether the resource exists
     * @param resourceName   the name of the resource being accessed (for error
     *                       message)
     * @throws ResponseStatusException with 404 Not Found if validation fails
     */
    public static void validateResourceExists(boolean resourceExists, String resourceName) {
        if (!resourceExists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, resourceName + " not found");
        }
    }
}

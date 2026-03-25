package ee.kaidokurm.ndl.export;

import ee.kaidokurm.ndl.auth.user.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearerAuth")
@RestController
public class DataExportController {

    private static final DateTimeFormatter FILE_TS_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final UserService userService;
    private final DataExportService dataExportService;

    public DataExportController(UserService userService, DataExportService dataExportService) {
        this.userService = userService;
        this.dataExportService = dataExportService;
    }

    @GetMapping(value = "/api/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DataExportService.DataExportResponse> exportUserData(JwtAuthenticationToken auth) {
        var user = userService.ensureUserFromJwt(auth);
        var response = dataExportService.exportForUser(user.getId());

        String timestamp = FILE_TS_FORMAT.format(OffsetDateTime.now(ZoneOffset.UTC));
        String fileName = "numbers-dont-lie-export-" + timestamp + ".json";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(response);
    }
}

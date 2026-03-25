package ee.kaidokurm.ndl.common.persistence.encryption;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;

@Converter
public class EncryptedStringListConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<String>> LIST_TYPE = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            String json = OBJECT_MAPPER.writeValueAsString(attribute);
            return EncryptionSupport.encrypt(json);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize encrypted list", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return List.of();
        }
        try {
            String json = EncryptionSupport.decrypt(dbData);
            return OBJECT_MAPPER.readValue(json, LIST_TYPE);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize encrypted list", e);
        }
    }
}

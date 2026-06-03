package org.example.contest.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.example.contest.config.AppProperties;
import org.example.contest.domain.UserAccount;
import org.example.contest.domain.enums.UserRole;
import org.springframework.stereotype.Service;

/**
 * 轻量 HS256 JWT 实现，减少额外安全库依赖；签名校验失败或过期均返回空。
 */
@Service
public class JwtService {
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;

    public JwtService(ObjectMapper objectMapper, AppProperties appProperties) {
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
    }

    public String issue(UserAccount user) {
        Instant expiresAt = Instant.now().plusSeconds(appProperties.getSecurity().getJwtExpirationMinutes() * 60);
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", user.getEmail());
        payload.put("uid", user.getId().toString());
        payload.put("role", user.getRole().name());
        payload.put("exp", expiresAt.getEpochSecond());
        String headerPart = encodeJson(header);
        String payloadPart = encodeJson(payload);
        String unsigned = headerPart + "." + payloadPart;
        return unsigned + "." + sign(unsigned);
    }

    public Optional<Claims> parse(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return Optional.empty();
            }
            String unsigned = parts[0] + "." + parts[1];
            if (!constantTimeEquals(sign(unsigned), parts[2])) {
                return Optional.empty();
            }
            Map<String, Object> payload = objectMapper.readValue(
                    URL_DECODER.decode(parts[1]),
                    new TypeReference<Map<String, Object>>() {}
            );
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() >= exp) {
                return Optional.empty();
            }
            return Optional.of(new Claims(
                    UUID.fromString((String) payload.get("uid")),
                    (String) payload.get("sub"),
                    UserRole.valueOf((String) payload.get("role")),
                    Instant.ofEpochSecond(exp)
            ));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private String encodeJson(Object value) {
        try {
            return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception exception) {
            throw new IllegalStateException("JWT 序列化失败", exception);
        }
    }

    private String sign(String unsigned) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    appProperties.getSecurity().getJwtSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));
            return URL_ENCODER.encodeToString(mac.doFinal(unsigned.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("JWT 签名失败", exception);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        return java.security.MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8)
        );
    }

    public record Claims(UUID userId, String email, UserRole role, Instant expiresAt) {
    }
}

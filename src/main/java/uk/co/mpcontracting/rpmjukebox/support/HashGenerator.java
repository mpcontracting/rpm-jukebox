package uk.co.mpcontracting.rpmjukebox.support;

import java.math.BigInteger;
import java.security.MessageDigest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HashGenerator {

  public String generateHash(Object... objects) throws Exception {
        log.debug("Generating hash for - {}", objectsAsString(objects));

        if (objects == null || objects.length == 0) {
            throw new IllegalArgumentException("Objects for hash generation must have at least one value");
        }

        try {
            StringBuilder builder = new StringBuilder();

            for (Object object : objects) {
                if (object == null) {
                    continue;
                }

                builder.append(object.toString());
            }

            if (builder.length() < 1) {
                throw new Exception("Hash generation string must have a length > 1");
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update(builder.toString().getBytes("UTF-8"));

            return toHex(digest.digest());
        } catch (Exception e) {
            throw new Exception("Error generating hash for - " + objectsAsString(objects), e);
        }
    }

  private String objectsAsString(Object... objects) {
        StringBuilder builder = new StringBuilder();

        if (objects != null) {
            for (Object object : objects) {
                if (object == null) {
                    builder.append("null");
                } else {
                    builder.append(object.toString());
                }

                builder.append(", ");
            }

            if (builder.length() > 0) {
                builder.setLength(builder.length() - 2);
            }
        } else {
            builder.append("null");
        }

        return builder.toString();
    }

  private String toHex(byte[] bytes) {
        return String.format("%0" + (bytes.length << 1) + "x", new BigInteger(1, bytes));
    }
}

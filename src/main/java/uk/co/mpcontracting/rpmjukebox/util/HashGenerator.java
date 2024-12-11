package uk.co.mpcontracting.rpmjukebox.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.math.BigInteger;
import java.security.MessageDigest;
import org.springframework.stereotype.Component;

@Component
public class HashGenerator {

  public String generateHash(Object... objects) {
    if (isNull(objects) || objects.length == 0) {
      throw new IllegalArgumentException("Objects for hash generation must have at least one value");
    }

    try {
      StringBuilder builder = new StringBuilder();

      for (Object object : objects) {
        if (isNull(object)) {
          continue;
        }

        builder.append(object);
      }

      if (builder.isEmpty()) {
        throw new Exception("Hash generation string must have a length > 1");
      }

      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.reset();
      digest.update(builder.toString().getBytes(UTF_8));

      return toHex(digest.digest());
    } catch (Exception e) {
      throw new RuntimeException("Error generating hash for - " + objectsAsString(objects), e);
    }
  }

  private String objectsAsString(Object... objects) {
    StringBuilder builder = new StringBuilder();

    if (nonNull(objects)) {
      for (Object object : objects) {
        if (isNull(object)) {
          builder.append("null");
        } else {
          builder.append(object);
        }

        builder.append(", ");
      }

      if (!builder.isEmpty()) {
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

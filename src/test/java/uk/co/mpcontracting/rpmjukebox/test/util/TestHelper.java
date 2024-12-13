package uk.co.mpcontracting.rpmjukebox.test.util;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

public final class TestHelper {

  private TestHelper() {}

  public static File getConfigDirectory() {
    return new File(System.getProperty("user.home") + File.separator + ".rpmjukeboxtest");
  }

  public static long getLocalDateTimeInMillis(LocalDateTime localDateTime) {
    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()).getTime();
  }

  public static long getDateTimeInMillis(int year, int month, int day, int hour, int minute) {
    return getLocalDateTimeInMillis(LocalDateTime.of(year, month, day, hour, minute));
  }

  @SneakyThrows
  public static File getTestResourceFile(String path) {
    return new ClassPathResource(path).getFile();
  }

  @SneakyThrows
  public static String getTestResourceContent(String path) {
    StringBuilder builder = new StringBuilder();

    try (BufferedReader reader = new BufferedReader(new FileReader(getTestResourceFile(path)))) {
      reader.lines().forEach(line -> {
        builder.append(line);
        builder.append("\r\n");
      });
    }

    return builder.toString();
  }

  public static void setField(Object object, Object field) {
    getAllFields(object).stream()
        .filter(f -> f.getType().equals(field.getClass()))
        .forEach(f -> setField(object, f.getName(), field));
  }

  public static void setField(Object object, String fieldName, Object field) {
    ReflectionTestUtils.setField(object, fieldName, field);
  }

  public static void setField(Class<?> targetClass, String fieldName, Object field) {
    ReflectionTestUtils.setField(targetClass, fieldName, field);
  }

  public static <T> T getField(Object object, Class<T> clazz) {
    return getAllFields(object).stream()
        .filter(f -> f.getType().equals(clazz))
        .findFirst()
        .map(f -> getField(object, f.getName(), clazz))
        .orElse(null);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getField(Object object, String fieldName, Class<T> clazz) {
    return (T) ReflectionTestUtils.getField(object, fieldName);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getField(Class<?> targetClass, String fieldName, Class<T> clazz) {
    return (T) ReflectionTestUtils.getField(targetClass, fieldName);
  }

  public static <T> T getNonNullField(Object object, String fieldName, Class<T> clazz) {
    return requireNonNull(getField(object, fieldName, clazz));
  }

  private static List<Field> getAllFields(Object object) {
    Class<?> clazz = object.getClass();
    List<Field> fields = new ArrayList<>();

    do {
      fields.addAll(List.of(clazz.getDeclaredFields()));
      clazz = clazz.getSuperclass();
    } while (nonNull(clazz));

    return fields;
  }
}

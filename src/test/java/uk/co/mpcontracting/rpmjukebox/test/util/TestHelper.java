package uk.co.mpcontracting.rpmjukebox.test.util;

import org.springframework.test.util.ReflectionTestUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class TestHelper {

  private TestHelper() {}

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

  private static List<Field> getAllFields(Object object) {
    Class<?> clazz = object.getClass();
    List<Field> fields = new ArrayList<>();

    do {
      fields.addAll(List.of(clazz.getDeclaredFields()));
      clazz = clazz.getSuperclass();
    } while (clazz != null);

    return fields;
  }
}

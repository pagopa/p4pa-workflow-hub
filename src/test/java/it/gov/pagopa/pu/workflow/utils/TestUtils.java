package it.gov.pagopa.pu.workflow.utils;

import org.junit.jupiter.api.Assertions;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class TestUtils {

  private TestUtils() {
  }

  /**
   * It will assert not null on all o's fields
   */
  public static void checkNotNullFields(Object o, String... excludedFields) {
    Set<String> excludedFieldsSet = new HashSet<>(Arrays.asList(excludedFields));
    org.springframework.util.ReflectionUtils.doWithFields(o.getClass(),
      f -> {
        f.setAccessible(true);
        Assertions.assertNotNull(f.get(o), "The field " + f.getName() + " of the input object of type " + o.getClass() + " is null!");
      },
      f -> !excludedFieldsSet.contains(f.getName()));
  }

  public static final Date DATE = Date.from(
    LocalDateTime.of(2024, 5, 15, 10, 30, 0)
      .atZone(ZoneId.systemDefault())
      .toInstant()
  );

  public static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.ofInstant(
    DATE.toInstant(),
    ZoneId.systemDefault());

}

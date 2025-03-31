package it.gov.pagopa.pu.workflow.utils;

import it.gov.pagopa.payhub.activities.util.Utilities;
import org.junit.jupiter.api.Assertions;
import uk.co.jemos.podam.api.*;
import uk.co.jemos.podam.common.ManufacturingContext;
import uk.co.jemos.podam.typeManufacturers.AbstractTypeManufacturer;

import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.Type;
import java.time.*;
import java.util.*;

public class TestUtils {

  private TestUtils() {
  }

  public static final LocalDate DATE = LocalDate.of(2024, 5, 15);
  public static final LocalDateTime DATETIME = LocalDateTime.of(DATE, LocalTime.of(10, 30, 0));
  public static final OffsetDateTime OFFSET_DATE_TIME = DATETIME.atZone(ZoneId.systemDefault()).toOffsetDateTime();

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

  public static PodamFactory getPodamFactory() {
    PodamFactory externalFactory = new AbstractExternalFactory() {
      @Override
      public <T> T manufacturePojo(Class<T> pojoClass, Type... genericTypeArgs) {
        if(pojoClass.isAssignableFrom(XMLGregorianCalendar.class)) {
          return (T) Utilities.toXMLGregorianCalendar(OffsetDateTime.now());
        }
        return null;
      }

      @Override
      public <T> T populatePojo(T pojo, Type... genericTypeArgs) {
        return null;
      }
    };
    PodamFactoryImpl podamFactory = new PodamFactoryImpl(externalFactory);
    podamFactory.getStrategy().addOrReplaceTypeManufacturer(SortedSet.class, new AbstractTypeManufacturer<>(){
      @Override
      public SortedSet<?> getType(DataProviderStrategy strategy, AttributeMetadata attributeMetadata, ManufacturingContext manufacturingCtx) {
        return new TreeSet<>();
      }
    });
    return podamFactory;
  }

}

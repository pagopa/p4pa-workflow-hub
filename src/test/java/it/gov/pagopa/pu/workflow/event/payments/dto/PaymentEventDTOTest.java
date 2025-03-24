package it.gov.pagopa.pu.workflow.event.payments.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PaymentEventDTOTest {

  private final Map<PaymentEventType, Class<? extends PaymentEventDTO<?>>> enum2ExpectedModel = Map.of(
    PaymentEventType.DP_CREATED, DebtPositionEventDTO.class,
    PaymentEventType.DP_UPDATED, DebtPositionEventDTO.class,
    PaymentEventType.DP_CANCELLED, DebtPositionEventDTO.class,
    PaymentEventType.DPI_ADDED, DebtPositionEventDTO.class,
    PaymentEventType.DPI_UPDATED, DebtPositionEventDTO.class,
    PaymentEventType.DPI_CANCELLED, DebtPositionEventDTO.class,
    PaymentEventType.RT_RECEIVED, DebtPositionEventDTO.class,
    PaymentEventType.SYNC_ERROR, DebtPositionEventDTO.class
  );

  @Test
  void testExpectedMapIsCompleted() {
    Assertions.assertEquals(
      Arrays.stream(PaymentEventType.values()).collect(Collectors.toSet()),
      enum2ExpectedModel.keySet()
    );
  }

  @SuppressWarnings("unchecked")
  @Test
  void testSubTypesConfig() {
    JsonSubTypes jsonSubTypes = PaymentEventDTO.class.getAnnotation(JsonSubTypes.class);
    Assertions.assertNotNull(jsonSubTypes);

    Assertions.assertEquals(enum2ExpectedModel,
      Arrays.stream(jsonSubTypes.value())
        .map(t -> (StringUtils.isNotBlank(t.name()) ? Stream.of(t.name()) : Arrays.stream(t.names()))
          .collect(Collectors.toMap(PaymentEventType::valueOf,
            n -> (Class<? extends PaymentEventDTO<?>>)t.value())))
        .reduce(new EnumMap<>(PaymentEventType.class), (acc, e) -> {
          acc.putAll(e);
          return acc;
        })
    );
  }
}

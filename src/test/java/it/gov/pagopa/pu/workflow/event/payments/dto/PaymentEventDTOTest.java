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

  private final Map<PaymentEventType, Class<? extends PaymentEventDTO<?>>> enum2ExpectedModel = Map.ofEntries(
    Map.entry(PaymentEventType.DP_CREATED, DebtPositionEventDTO.class),
    Map.entry(PaymentEventType.DP_UPDATED, DebtPositionEventDTO.class),
    Map.entry(PaymentEventType.DP_CANCELLED, DebtPositionEventDTO.class),
    Map.entry(PaymentEventType.DPI_ADDED, DebtPositionEventDTO.class),
    Map.entry(PaymentEventType.DPI_UPDATED, DebtPositionEventDTO.class),
    Map.entry(PaymentEventType.DPI_CANCELLED, DebtPositionEventDTO.class),
    Map.entry(PaymentEventType.RT_RECEIVED, DebtPositionEventDTO.class),
    Map.entry(PaymentEventType.SYNC_ERROR, DebtPositionEventDTO.class),

    Map.entry(PaymentEventType.IO_NOTIFIED, DebtPositionIoEventDTO.class),

    Map.entry(PaymentEventType.SEND_NOTIFICATION_CREATED, DebtPositionSendEventDTO.class),
    Map.entry(PaymentEventType.SEND_NOTIFICATION_DATE, DebtPositionSendEventDTO.class),
    Map.entry(PaymentEventType.SEND_NOTIFICATION_ERROR, DebtPositionSendEventDTO.class)
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

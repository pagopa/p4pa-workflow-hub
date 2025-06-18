package it.gov.pagopa.pu.workflow.utilities;

import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import java.util.EnumSet;
import java.util.Set;

public class PaymentEventTypeUtils {

  private PaymentEventTypeUtils() {}

  public static final Set<PaymentEventType> CREATE_OR_UPDATE_STATUSES = EnumSet.of(
    PaymentEventType.DPI_ADDED,
    PaymentEventType.DP_CREATED,
    PaymentEventType.DP_UPDATED,
    PaymentEventType.DPI_UPDATED
  );
}

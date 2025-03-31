package it.gov.pagopa.pu.workflow.service.debtposition.sync.partial;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DebtPositionPartialChangeSyncServiceTest {

  private final DebtPositionPartialChangeSyncService service = new DebtPositionPartialChangeSyncService();

  @Test
  void whenInvokeWorkflowThenDoNothingAndReturnNull(){
    Assertions.assertNull(service.invokeWorkflow(new DebtPositionDTO(), new PaymentEventRequestDTO(PaymentEventType.DP_CREATED, null)));
  }
}

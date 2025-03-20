package it.gov.pagopa.pu.workflow.service.debtposition.sync.complete.custom;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.WfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DebtPositionCustomWfSyncServiceTest {

  private final DebtPositionCustomWfSyncService service = new DebtPositionCustomWfSyncService();

  @Test
  void whenInvokeWorkflowThenDoNothingAndReturnNull(){
    Assertions.assertNull(service.invokeWorkflow(new DebtPositionDTO(), PaymentEventType.DP_CREATED, new WfExecutionParameters(false, false, Mockito.mock(WfExecutionConfig.class))));
  }
}

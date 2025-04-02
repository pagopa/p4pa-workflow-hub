package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.activity;

import it.gov.pagopa.payhub.activities.dto.IONotificationMessage;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.SynchronizeDebtPositionWfClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class InvokeSyncDebtPositionActivityTest {

  @Mock
  private SynchronizeDebtPositionWfClient synchronizeDebtPositionWfClientMock;

  private InvokeSyncDebtPositionActivity activity;

  @BeforeEach
  void init() {
    activity = new InvokeSyncDebtPositionActivityImpl(synchronizeDebtPositionWfClientMock);
  }

  @Test
  void whenSynchronizeDPSyncThenOk(){
    // Given
    PaymentEventRequestDTO paymentEventRequestDTO = new PaymentEventRequestDTO(PaymentEventType.IO_NOTIFIED, "description");
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    GenericWfExecutionConfig wfExecutionConfig =
      new GenericWfExecutionConfig(new GenericWfExecutionConfig.IONotificationBaseOpsMessages(new IONotificationMessage("subject", "message"), null, null));
    String workflowId = "workflowId";

    Mockito.when(synchronizeDebtPositionWfClientMock.synchronizeDPSync(debtPositionDTO, paymentEventRequestDTO, wfExecutionConfig))
      .thenReturn(workflowId);

    // When
    String result = activity.synchronizeDPSync(debtPositionDTO, paymentEventRequestDTO, wfExecutionConfig);

    // Then
    assertEquals(workflowId, result);
  }
}

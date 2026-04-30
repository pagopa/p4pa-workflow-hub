package it.gov.pagopa.pu.workflow.wf.debtposition.custom.activity;

import it.gov.pagopa.payhub.activities.connector.auth.AuthnService;
import it.gov.pagopa.payhub.activities.dto.IONotificationMessage;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.wf.debtposition.sync.complete.generic.DebtPositionGenericSyncService;
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
  private DebtPositionGenericSyncService debtPositionGenericSyncServiceMock;
  @Mock
  private AuthnService authnService;

  private InvokeSyncDebtPositionActivity activity;

  @BeforeEach
  void init() {
    activity = new InvokeSyncDebtPositionActivityImpl(debtPositionGenericSyncServiceMock, authnService);
  }

  @Test
  void whenSynchronizeDPSyncThenOk(){
    // Given
    PaymentEventRequestDTO paymentEventRequestDTO = new PaymentEventRequestDTO(PaymentEventType.IO_NOTIFIED, "description");
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    GenericWfExecutionConfig wfExecutionConfig =
      new GenericWfExecutionConfig(new GenericWfExecutionConfig.IONotificationBaseOpsMessages(new IONotificationMessage("subject", "message"), null, null));
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");
    String accessToken = "accessToken";

    Mockito.when(authnService.getAccessToken()).thenReturn(accessToken);

    Mockito.when(debtPositionGenericSyncServiceMock.invokeWorkflow(debtPositionDTO, paymentEventRequestDTO, false, wfExecutionConfig, accessToken))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = activity.synchronizeDPSync(debtPositionDTO, paymentEventRequestDTO, false, wfExecutionConfig);

    // Then
    assertEquals(expectedResult, result);
  }
}

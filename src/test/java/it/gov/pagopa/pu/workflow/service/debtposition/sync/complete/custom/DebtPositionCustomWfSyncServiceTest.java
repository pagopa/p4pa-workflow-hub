package it.gov.pagopa.pu.workflow.service.debtposition.sync.complete.custom;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.payhub.activities.dto.IONotificationMessage;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.WfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.DebtPositionFineClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtPositionCustomWfSyncServiceTest {

  @Mock
  private DebtPositionFineClient fineClientMock;

  private DebtPositionCustomWfSyncService service;

  @BeforeEach
  void init() {
    service = new DebtPositionCustomWfSyncService(fineClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(fineClientMock);
  }

  @Test
  void whenInvokeWorkflowThenOk() {
    // Given
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    FineWfExecutionConfig.IONotificationFineWfMessages fineWfMessages =
      new FineWfExecutionConfig.IONotificationFineWfMessages(new IONotificationMessage("subject", "message"), new IONotificationMessage("subject", "message"));

    FineWfExecutionConfig wfExecutionConfig = new FineWfExecutionConfig();
    wfExecutionConfig.setIoMessages(fineWfMessages);
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO(PaymentEventType.IO_NOTIFIED, "description");

    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    wfExecutionParameters.setMassive(false);
    wfExecutionParameters.setWfExecutionConfig(wfExecutionConfig);

    when(fineClientMock.synchronizeFineDP(debtPositionDTO, paymentEventRequest, false, wfExecutionConfig))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = service.invokeWorkflow(debtPositionDTO, paymentEventRequest, wfExecutionParameters);

    // Then
    assertEquals(expectedResult, result);
    verify(fineClientMock).synchronizeFineDP(debtPositionDTO, paymentEventRequest, false, wfExecutionConfig);
  }

  @Test
  void givenNotFineWfExecutionConfigWhenInvokeWorkflowThenThrowIllegalStateException() {
    // Given
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO(PaymentEventType.IO_NOTIFIED, "description");
    WfExecutionConfig genericConfig = new GenericWfExecutionConfig();
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    wfExecutionParameters.setMassive(true);
    wfExecutionParameters.setWfExecutionConfig(genericConfig);

    // When & Then
    assertThrows(IllegalStateException.class,
      () -> service.invokeWorkflow(debtPositionDTO, paymentEventRequest, wfExecutionParameters));
  }

}

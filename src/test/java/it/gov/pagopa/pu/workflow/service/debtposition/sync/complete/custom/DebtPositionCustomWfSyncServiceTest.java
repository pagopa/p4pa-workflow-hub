package it.gov.pagopa.pu.workflow.service.debtposition.sync.complete.custom;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.payhub.activities.dto.IONotificationMessage;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.WfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.DebtPositionFineClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    String workflowId = "workflowId";
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    FineWfExecutionConfig.IONotificationFineWfMessages fineWfMessages =
      new FineWfExecutionConfig.IONotificationFineWfMessages(null, new IONotificationMessage("subject", "message"));

    FineWfExecutionConfig wfExecutionConfig = new FineWfExecutionConfig();
    wfExecutionConfig.setIoMessages(fineWfMessages);
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO();

    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    wfExecutionParameters.setMassive(false);
    wfExecutionParameters.setWfExecutionConfig(wfExecutionConfig);

    when(fineClientMock.synchronizeFine(debtPositionDTO, paymentEventRequest, false, wfExecutionConfig))
      .thenReturn(workflowId);

    // When
    String result = service.invokeWorkflow(debtPositionDTO, paymentEventRequest, wfExecutionParameters);

    // Then
    assertEquals(workflowId, result);
    verify(fineClientMock).synchronizeFine(debtPositionDTO, paymentEventRequest, false, wfExecutionConfig);
  }

  @Test
  void givenNotFineWfExecutionConfigWhenInvokeWorkflowThenDoNothingAndReturnNull() {
    // Given
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO();
    WfExecutionConfig genericConfig = new GenericWfExecutionConfig();
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    wfExecutionParameters.setMassive(true);
    wfExecutionParameters.setWfExecutionConfig(genericConfig);

    // When
    String result = service.invokeWorkflow(debtPositionDTO, paymentEventRequest, wfExecutionParameters);

    // Then
    assertNull(result);
  }
}

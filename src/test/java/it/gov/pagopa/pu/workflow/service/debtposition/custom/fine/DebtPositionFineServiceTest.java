package it.gov.pagopa.pu.workflow.service.debtposition.custom.fine;

import it.gov.pagopa.payhub.activities.dto.IONotificationMessage;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DebtPositionFineServiceTest {

  @Mock
  private DebtPositionFineClient debtPositionFineClientMock;

  private DebtPositionFineService service;

  @BeforeEach
  void init(){
    service = new DebtPositionFineServiceImpl(debtPositionFineClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(debtPositionFineClientMock);
  }

  @Test
  void whenHandleFineReductionExpiration(){
    // Given
    Long debtPositionId = 1L;
    String accessToken = "accessToken";
    String expectedWorkflowId = "FineReductionOptionExpirationWF-1";
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO(PaymentEventType.DP_CREATED, null);
    FineWfExecutionConfig.IONotificationFineWfMessages fineWfMessages =
      new FineWfExecutionConfig.IONotificationFineWfMessages(null, new IONotificationMessage("subject", "message"));

    FineWfExecutionConfig wfExecutionConfig = new FineWfExecutionConfig();
    wfExecutionConfig.setIoMessages(fineWfMessages);

    Mockito.when(debtPositionFineClientMock.handleFineReductionExpiration(debtPositionId, paymentEventRequest, false, wfExecutionConfig, accessToken))
      .thenReturn(expectedWorkflowId);

    // When
    WorkflowCreatedDTO workflowCreatedDTO = service.handleFineReductionExpiration(debtPositionId, paymentEventRequest, false, wfExecutionConfig, accessToken);

    // Then
    assertEquals(expectedWorkflowId, workflowCreatedDTO.getWorkflowId());
  }
}

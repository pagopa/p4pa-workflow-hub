package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine;

import it.gov.pagopa.payhub.activities.dto.IONotificationMessage;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfreductionexpiration.FineReductionOptionExpirationWF;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfreductionexpiration.FineReductionOptionExpirationWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class DebtPositionFineClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private FineReductionOptionExpirationWF fineReductionOptionExpirationWFMock;

  private DebtPositionFineClient client;

  @BeforeEach
  void init(){
    client = new DebtPositionFineClientImpl(workflowServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void whenHandleFineReductionExpirationThenSuccess() {
    // Given
    Long debtPositionId = 1L;
    String taskQueue = FineReductionOptionExpirationWFImpl.TASK_QUEUE_FINE_REDUCTION_OPTION_EXPIRATION;
    String expectedWorkflowId = "FineReductionOptionExpirationWF-1";
    PaymentEventRequestDTO paymentEventRequestDTO = new PaymentEventRequestDTO(PaymentEventType.IO_NOTIFIED, "description");
    FineWfExecutionConfig.IONotificationFineWfMessages fineWfMessages =
      new FineWfExecutionConfig.IONotificationFineWfMessages(null, new IONotificationMessage("subject", "message"));

    FineWfExecutionConfig wfExecutionConfig = new FineWfExecutionConfig();
    wfExecutionConfig.setIoMessages(fineWfMessages);

    try (MockedStatic<Utilities> utilitiesMockedStatic = mockStatic(Utilities.class)) {
      utilitiesMockedStatic
        .when(() -> Utilities.generateWorkflowId(debtPositionId, taskQueue))
        .thenReturn(expectedWorkflowId);

      Mockito.when(workflowServiceMock.buildWorkflowStub(
          FineReductionOptionExpirationWF.class,
          taskQueue,
          expectedWorkflowId))
        .thenReturn(fineReductionOptionExpirationWFMock);

      // When
      String workflowId = client.handleFineReductionExpiration(debtPositionId, paymentEventRequestDTO, wfExecutionConfig);

      // Then
      Assertions.assertEquals(expectedWorkflowId, workflowId);
      Mockito.verify(fineReductionOptionExpirationWFMock).handleFineReductionExpiration(debtPositionId, paymentEventRequestDTO, wfExecutionConfig);
    }
  }
}

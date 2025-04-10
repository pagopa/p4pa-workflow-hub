package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification;

import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.wfingestion.PaymentNotificationIngestionWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification.wfingestion.PaymentNotificationIngestionWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentNotificationIngestionWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private PaymentNotificationIngestionWFImpl wfMock;

  private PaymentNotificationIngestionWFClient client;

  @BeforeEach
  void init() {
    client = new PaymentNotificationIngestionWFClient(workflowServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void whenIngestThenOk() {
    // Given
    long ingestionFlowFileId = 1L;
    String taskQueue = PaymentNotificationIngestionWFImpl.TASK_QUEUE_PAYMENT_NOTIFICATION_INGESTION_WF;
    String expectedWorkflowId = "PaymentNotificationIngestionWF-1";

    Mockito.when(workflowServiceMock.buildWorkflowStub(PaymentNotificationIngestionWF.class, taskQueue, expectedWorkflowId))
      .thenReturn(wfMock);

    // When
    String workflowId = client.ingest(ingestionFlowFileId);

    // Then
    Assertions.assertEquals(expectedWorkflowId, workflowId);
    Mockito.verify(wfMock).ingest(ingestionFlowFileId);
  }
}

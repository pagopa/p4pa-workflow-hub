package it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentnotification;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
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
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private PaymentNotificationIngestionWFImpl wfMock;

  private PaymentNotificationIngestionWFClient client;

  @BeforeEach
  void init() {
    client = new PaymentNotificationIngestionWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void whenIngestThenOk() {
    // Given
    long ingestionFlowFileId = 1L;
    String taskQueue = PaymentNotificationIngestionWFImpl.TASK_QUEUE_PAYMENT_NOTIFICATION_INGESTION_WF;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("PaymentNotificationIngestionWF-1", "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStub(PaymentNotificationIngestionWF.class, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(wfMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, ingestionFlowFileId);

    // When
    WorkflowCreatedDTO result = client.ingest(ingestionFlowFileId);

    // Then
    Assertions.assertEquals(expectedResult, result);
    Mockito.verify(wfMock).ingest(ingestionFlowFileId);
  }
}

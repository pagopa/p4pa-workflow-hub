package it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa.wfingestion.ReceiptPagopaIngestionWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa.wfingestion.ReceiptPagopaIngestionWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReceiptPagopaIngestionWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private ReceiptPagopaIngestionWF wfMock;

  private ReceiptPagopaIngestionWFClient client;

  @BeforeEach
  void setUp() {
    client = new ReceiptPagopaIngestionWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void whenIngestThenOk() {
    // Given
    long ingestionFlowFileId = 1L;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("ReceiptPagopaIngestionWF-1", "RUNID");

    String taskQueue = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY;
    doReturn(wfMock).when(workflowServiceMock)
      .buildWorkflowStub(ReceiptPagopaIngestionWF.class, taskQueue, expectedResult.getWorkflowId());

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, ingestionFlowFileId);

    // When
    WorkflowCreatedDTO result = client.ingest(ingestionFlowFileId);

    // Then
    assertEquals(expectedResult, result);
    verify(wfMock).ingest(ingestionFlowFileId);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, ReceiptPagopaIngestionWFImpl.class);
  }
}

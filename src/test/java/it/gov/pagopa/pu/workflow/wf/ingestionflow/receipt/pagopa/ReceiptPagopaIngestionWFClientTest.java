package it.gov.pagopa.pu.workflow.wf.ingestionflow.receipt.pagopa;

import it.gov.pagopa.pu.workflow.service.WorkflowService;
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
  private ReceiptPagopaIngestionWF wfMock;

  private ReceiptPagopaIngestionWFClient client;

  @BeforeEach
  void setUp() {
    client = new ReceiptPagopaIngestionWFClient(workflowServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void whenIngestThenOk() {
    // Given
    long ingestionFlowFileId = 1L;
    String expectedWorkflowId = "ReceiptPagopaIngestionWF-1";

    doReturn(wfMock).when(workflowServiceMock)
      .buildWorkflowStub(ReceiptPagopaIngestionWF.class, ReceiptPagopaIngestionWFImpl.TASK_QUEUE_RECEIPT_PAGOPA_INGESTION_WF, expectedWorkflowId);

    // When
    String workflowId = client.ingest(ingestionFlowFileId);

    // Then
    assertEquals(expectedWorkflowId, workflowId);
    verify(wfMock).ingest(ingestionFlowFileId);
  }
}

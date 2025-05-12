package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.wfingestion.DebtPositionIngestionFlowWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtposition.wfingestion.DebtPositionIngestionFlowWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DebtPositionIngestionWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private DebtPositionIngestionFlowWF wfMock;

  private DebtPositionIngestionWFClient client;

  @BeforeEach
  void init() {
    client = new DebtPositionIngestionWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void whenIngestThenOk() {
    // Given
    long ingestionFlowFileId = 1L;
    String taskQueue = DebtPositionIngestionFlowWFImpl.TASK_QUEUE_DEBT_POSITION_INGESTION_FLOW;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("DebtPositionIngestionFlowWF-1", "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStub(DebtPositionIngestionFlowWF.class, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(wfMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, ingestionFlowFileId);

    // When
    WorkflowCreatedDTO result = client.ingest(ingestionFlowFileId);

    // Then
    Assertions.assertEquals(expectedResult, result);
    Mockito.verify(wfMock).ingest(ingestionFlowFileId);
  }
}

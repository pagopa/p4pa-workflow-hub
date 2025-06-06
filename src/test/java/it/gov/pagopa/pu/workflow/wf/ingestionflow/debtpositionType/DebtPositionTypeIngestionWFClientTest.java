package it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositionType;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontype.DebtPositionTypeIngestionWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontype.wfingestion.DebtPositionTypeIngestionWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.debtpositiontype.wfingestion.DebtPositionTypeIngestionWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeIngestionWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private DebtPositionTypeIngestionWF wfMock;

  private DebtPositionTypeIngestionWFClient client;

  @BeforeEach
  void init() {
    client = new DebtPositionTypeIngestionWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void whenIngestThenOk() {
    // Given
    long ingestionFlowFileId = 1L;
    String taskQueue = DebtPositionTypeIngestionWFImpl.TASK_QUEUE_DEBT_POSITION_TYPE_INGESTION_WF;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("DebtPositionTypeIngestionWF-1", "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStub(DebtPositionTypeIngestionWF.class, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(wfMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, ingestionFlowFileId);

    // When
    WorkflowCreatedDTO result = client.ingest(ingestionFlowFileId);

    // Then
    Assertions.assertEquals(expectedResult, result);
    Mockito.verify(wfMock).ingest(ingestionFlowFileId);
  }
}

package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.poste;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.poste.wfingestion.TreasuryPosteIngestionWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.poste.wfingestion.TreasuryPosteIngestionWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TreasuryPosteIngestionWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private TreasuryPosteIngestionWF wfMock;

  private TreasuryPosteIngestionWFClient client;

  @BeforeEach
  void init() {
    client = new TreasuryPosteIngestionWFClient(workflowServiceMock,
      workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock,
      workflowClientServiceMock);
  }

  @Test
  void whenIngestThenOk() {
    // Given
    long ingestionFlowFileId = 1L;
    String taskQueue = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO(
      "TreasuryPosteIngestionWF-1", "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStub(
        TreasuryPosteIngestionWF.class, taskQueue,
        expectedResult.getWorkflowId()))
      .thenReturn(wfMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(
      workflowClientServiceMock, expectedResult, ingestionFlowFileId);

    // When
    WorkflowCreatedDTO result = client.ingest(ingestionFlowFileId);

    // Then
    Assertions.assertEquals(expectedResult, result);
    Mockito.verify(wfMock).ingest(ingestionFlowFileId);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue,
      TreasuryPosteIngestionWFImpl.class);
  }
}

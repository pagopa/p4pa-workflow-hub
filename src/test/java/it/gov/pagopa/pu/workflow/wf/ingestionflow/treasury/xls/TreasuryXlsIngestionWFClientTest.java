package it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.xls;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.xls.wfingestion.TreasuryXlsIngestionWF;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.treasury.xls.wfingestion.TreasuryXlsIngestionWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TreasuryXlsIngestionWFClientTest {
  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private TreasuryXlsIngestionWF wfMock;

  private TreasuryXlsIngestionWFClient client;

  @BeforeEach
  void init() {
    client = new TreasuryXlsIngestionWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void givenIngestionFileIdWhenIngestThenOk() {
    long ingestionFlowFileId = 1L;
    String taskQueue = TaskQueueConstants.TASK_QUEUE_IMPORT_MEDIUM_PRIORITY;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("TreasuryXlsIngestionWF-1", "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStub(TreasuryXlsIngestionWF.class, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(wfMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, ingestionFlowFileId);

    WorkflowCreatedDTO result = client.ingest(ingestionFlowFileId);

    Assertions.assertEquals(expectedResult, result);
    Mockito.verify(wfMock).ingest(ingestionFlowFileId);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, TreasuryXlsIngestionWFImpl.class);
  }
}

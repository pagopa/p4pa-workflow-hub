package it.gov.pagopa.pu.workflow.wf.exportfile.export;

import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile.ExportFileTypeEnum;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.exportfile.export.wfexportfile.ExportFileWF;
import it.gov.pagopa.pu.workflow.wf.exportfile.export.wfexportfile.ExportFileWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExportFileWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private ExportFileWF wfMock;

  private ExportFileWFClient client;

  @BeforeEach
  void init() {
    client = new ExportFileWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void whenIngestThenOk() {
    // Given
    Long exportFileId = 1L;
    ExportFileTypeEnum exportFileType = ExportFileTypeEnum.PAID;
    String taskQueue = TaskQueueConstants.TASK_QUEUE_EXPORT_MEDIUM_PRIORITY;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("ExportFileWF-" + exportFileType + "-" + exportFileId, "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStubToStartNew(ExportFileWF.class, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(wfMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, exportFileId, exportFileType);

    // When
    WorkflowCreatedDTO result = client.exportFile(exportFileId, exportFileType);

    // Then
    Assertions.assertEquals(expectedResult, result);
    Mockito.verify(wfMock).exportFile(exportFileId, exportFileType);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, ExportFileWFImpl.class);
  }
}

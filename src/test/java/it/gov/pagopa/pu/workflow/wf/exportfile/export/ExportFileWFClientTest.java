package it.gov.pagopa.pu.workflow.wf.exportfile.export;

import static org.mockito.Mockito.mockStatic;

import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile.ExportFileTypeEnum;
import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.exportfile.export.wfexportfile.ExportFileWF;
import it.gov.pagopa.pu.workflow.wf.exportfile.export.wfexportfile.ExportFileWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExportFileWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private ExportFileWF wfMock;

  private ExportFileWFClient client;

  @BeforeEach
  void init(){
    client = new ExportFileWFClient(workflowServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void whenIngestThenOk(){
    // Given
    Long exportFileId = 1L;
    ExportFileTypeEnum exportFileType = ExportFileTypeEnum.PAID;
    String taskQueue = ExportFileWFImpl.TASK_QUEUE_EXPORT_FILE_WF;
    String expectedWorkflowId = "DebtPositionIngestionWF-"+exportFileType+"-"+exportFileId;

    try (MockedStatic<Utilities> utilitiesMockedStatic = mockStatic(Utilities.class)) {
      utilitiesMockedStatic
        .when(() -> Utilities.generateWorkflowId(exportFileType+"-"+exportFileId, taskQueue))
        .thenReturn(expectedWorkflowId);

      Mockito.when(workflowServiceMock.buildWorkflowStub(ExportFileWF.class, taskQueue, expectedWorkflowId))
        .thenReturn(wfMock);

      // When
      String workflowId = client.exportFile(exportFileId,exportFileType);

      // Then
      Assertions.assertEquals(expectedWorkflowId, workflowId);
      Mockito.verify(wfMock).exportFile(exportFileId,exportFileType);
    }
  }
}

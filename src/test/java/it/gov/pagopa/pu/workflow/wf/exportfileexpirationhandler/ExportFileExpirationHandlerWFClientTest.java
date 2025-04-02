package it.gov.pagopa.pu.workflow.wf.exportfileexpirationhandler;

import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.exportfileexpirationhandler.wfexportfileexpirationhandler.ExportFileExpirationHandlerHandlerWFImpl;
import it.gov.pagopa.pu.workflow.wf.exportfileexpirationhandler.wfexportfileexpirationhandler.ExportFileExpirationHandlerWF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExportFileExpirationHandlerWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private ExportFileExpirationHandlerWF wfMock;

  private ExportFileExpirationHandlerWFClient client;

  @BeforeEach
  void setUp() {
    client = new ExportFileExpirationHandlerWFClient(workflowServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void testCreateExportFileExpirationHandler() {
    Long exportFileId = 456L;
    String taskQueue = ExportFileExpirationHandlerHandlerWFImpl.TASK_QUEUE_EXPORT_FILE_EXPIRATION_HANDLER_WF;
    String expectedWorkflowId = "ExportFileExpirationHandlerWF-456";

    try (MockedStatic<Utilities> utilitiesMockedStatic = mockStatic(Utilities.class)) {
      utilitiesMockedStatic
        .when(() -> Utilities.generateWorkflowId(exportFileId, taskQueue))
        .thenReturn(expectedWorkflowId);

      Mockito.when(workflowServiceMock.buildWorkflowStub(ExportFileExpirationHandlerWF.class, taskQueue, expectedWorkflowId))
        .thenReturn(wfMock);

      String workflowId = client.exportFileExpirationHandler(exportFileId);

      assertEquals(expectedWorkflowId, workflowId);
      verify(wfMock).exportFileExpirationHandler(exportFileId);
    }
  }
}

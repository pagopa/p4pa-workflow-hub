package it.gov.pagopa.pu.workflow.wf.exportfile.expiration;

import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.exportfile.expiration.wfexpiration.ExportFileExpirationHandlerWF;
import it.gov.pagopa.pu.workflow.wf.exportfile.expiration.wfexpiration.ExportFileExpirationHandlerWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    String taskQueue = ExportFileExpirationHandlerWFImpl.TASK_QUEUE_EXPORT_FILE_EXPIRATION_HANDLER_WF;
    String expectedWorkflowId = "ExportFileExpirationHandlerWF-456";

    Mockito.when(workflowServiceMock.buildWorkflowStub(ExportFileExpirationHandlerWF.class, taskQueue, expectedWorkflowId))
      .thenReturn(wfMock);

    String workflowId = client.exportFileExpirationHandler(exportFileId);

    assertEquals(expectedWorkflowId, workflowId);
    verify(wfMock).exportFileExpirationHandler(exportFileId);
  }

  @Test
  void givenParamsWhenScheduleExportFileExpirationThenOk() {
    //given
    Long exportFileId = 1L;
    LocalDate dateTime = LocalDate.of(2025, 1, 1);
    String taskQueue = ExportFileExpirationHandlerWFImpl.TASK_QUEUE_EXPORT_FILE_EXPIRATION_HANDLER_WF;

    String expectedWorkflowId = "ExportFileExpirationHandlerWF-" + exportFileId;

    Mockito.when(workflowServiceMock.buildWorkflowStubScheduled(ExportFileExpirationHandlerWF.class, taskQueue, expectedWorkflowId, dateTime))
      .thenReturn(wfMock);
    //when
    client.scheduleExportFileExpiration(exportFileId, dateTime);
    //then
    verify(wfMock).exportFileExpirationHandler(exportFileId);
  }
}

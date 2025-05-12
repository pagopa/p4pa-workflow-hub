package it.gov.pagopa.pu.workflow.wf.exportfile.expiration;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
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
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private ExportFileExpirationHandlerWF wfMock;

  private ExportFileExpirationHandlerWFClient client;

  @BeforeEach
  void setUp() {
    client = new ExportFileExpirationHandlerWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void testCreateExportFileExpirationHandler() {
    Long exportFileId = 456L;
    String taskQueue = ExportFileExpirationHandlerWFImpl.TASK_QUEUE_EXPORT_FILE_EXPIRATION_HANDLER_WF;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("ExportFileExpirationHandlerWF-456", "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStub(ExportFileExpirationHandlerWF.class, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(wfMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, exportFileId);

    WorkflowCreatedDTO result = client.exportFileExpirationHandler(exportFileId);

    assertEquals(expectedResult, result);
    verify(wfMock).exportFileExpirationHandler(exportFileId);
  }

  @Test
  void givenParamsWhenScheduleExportFileExpirationThenOk() {
    //given
    Long exportFileId = 1L;
    LocalDate dateTime = LocalDate.of(2025, 1, 1);
    String taskQueue = ExportFileExpirationHandlerWFImpl.TASK_QUEUE_EXPORT_FILE_EXPIRATION_HANDLER_WF;

    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("ExportFileExpirationHandlerWF-" + exportFileId, "runId");

    Mockito.when(workflowServiceMock.buildWorkflowStubScheduled(ExportFileExpirationHandlerWF.class, taskQueue, expectedResult.getWorkflowId(), dateTime))
      .thenReturn(wfMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, exportFileId);

    //when
    client.scheduleExportFileExpiration(exportFileId, dateTime);
    //then
    verify(wfMock).exportFileExpirationHandler(exportFileId);
  }
}

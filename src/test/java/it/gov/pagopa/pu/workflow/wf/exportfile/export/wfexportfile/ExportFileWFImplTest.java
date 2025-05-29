package it.gov.pagopa.pu.workflow.wf.exportfile.export.wfexportfile;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.exportflow.ExportFileActivity;
import it.gov.pagopa.payhub.activities.activity.exportflow.UpdateExportFileStatusActivity;
import it.gov.pagopa.payhub.activities.dto.exportflow.ExportFileResult;
import it.gov.pagopa.payhub.activities.dto.exportflow.UpdateStatusRequest;
import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile.ExportFileTypeEnum;
import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFileStatus;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.exportfile.export.activity.ScheduleExportFileExpirationActivity;
import it.gov.pagopa.pu.workflow.wf.exportfile.export.config.ExportFileWFConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class ExportFileWFImplTest {

  @Mock
  private ExportFileActivity exportFileActivityMock;
  @Mock
  private UpdateExportFileStatusActivity updateExportFileStatusActivityMock;
  @Mock
  private ScheduleExportFileExpirationActivity scheduleExportFileExpirationActivityMock;

  private ExportFileWFImpl wf;
  private final int expirationDays = 2;

  @BeforeEach
  void setUp() {
    ExportFileWFConfig exportFileWFConfigMock = Mockito.mock(
      ExportFileWFConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(exportFileWFConfigMock.buildExportFileActivityStub())
      .thenReturn(exportFileActivityMock);
    Mockito.when(exportFileWFConfigMock.buildUpdateExportFileStatusActivityStub())
      .thenReturn(updateExportFileStatusActivityMock);
    Mockito.when(exportFileWFConfigMock.buildScheduleExportFileExpirationActivityStub())
      .thenReturn(scheduleExportFileExpirationActivityMock);

    Mockito.when(applicationContextMock.getBean(ExportFileWFConfig.class))
      .thenReturn(exportFileWFConfigMock);

    wf = new ExportFileWFImpl();
    wf.setApplicationContext(applicationContextMock);
    ReflectionTestUtils.setField(wf,"expirationDays",expirationDays);
  }

  @Test
  void givenSuccessfulFileCreationWhenExportFileThenOk() {
    Long exportFileId = 1L;
    ExportFileTypeEnum exportFileType = ExportFileTypeEnum.PAID;
    ExportFileResult exportFileResult = new ExportFileResult();
    exportFileResult.setFilePath("filePath");
    exportFileResult.setFileName("fileName");
    exportFileResult.setExportedRows(10L);
    exportFileResult.setFileSize(12L);
    exportFileResult.setExportDate(LocalDate.now());
    LocalDate expectedDueDate = exportFileResult.getExportDate().plusDays(expirationDays);
    UpdateStatusRequest processingUpdateStatusRequest = new UpdateStatusRequest(
      exportFileId,
      ExportFileStatus.REQUESTED, ExportFileStatus.PROCESSING, null, null,
      null,null,null, Utilities.toOffsetDateTimeEndOfTheDay(expectedDueDate));
    UpdateStatusRequest completedUpdateStatusRequest = new UpdateStatusRequest(
      exportFileId,
      ExportFileStatus.PROCESSING, ExportFileStatus.COMPLETED,
      exportFileResult.getFilePath(), exportFileResult.getFileName(),12L,
      exportFileResult.getExportedRows(),null, Utilities.toOffsetDateTimeEndOfTheDay(expectedDueDate));

    Mockito.doNothing().when(updateExportFileStatusActivityMock).updateStatus(Mockito.any());
    Mockito.when(exportFileActivityMock.executeExport(exportFileId,exportFileType)).thenReturn(exportFileResult);

    Mockito.doNothing().when(scheduleExportFileExpirationActivityMock).scheduleExportFileExpiration(exportFileId,
      expectedDueDate);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.exportFile(exportFileId,exportFileType);

      InOrder inOrder = Mockito.inOrder(updateExportFileStatusActivityMock, exportFileActivityMock);
      inOrder.verify(updateExportFileStatusActivityMock).updateStatus(Mockito.argThat(p->
        p.getExportFileId()
          .equals(processingUpdateStatusRequest.getExportFileId())
          && p.getOldStatus().equals(processingUpdateStatusRequest.getOldStatus())
          && p.getNewStatus().equals(processingUpdateStatusRequest.getNewStatus())
          && p.getFileName() == null
          && p.getFilePathName() == null
          && p.getFileSize() == null
          && p.getExportedRows() == null
          && p.getErrorDescription() == null
          && p.getExpirationDate() == null
      ));
      inOrder.verify(exportFileActivityMock).executeExport(exportFileId,exportFileType);
      inOrder.verify(updateExportFileStatusActivityMock).updateStatus(Mockito.argThat(p->
        p.getExportFileId()
          .equals(completedUpdateStatusRequest.getExportFileId())
          && p.getOldStatus().equals(completedUpdateStatusRequest.getOldStatus())
          && p.getNewStatus().equals(completedUpdateStatusRequest.getNewStatus())
          && p.getFileName().equals(completedUpdateStatusRequest.getFileName())
          && p.getFileSize().equals(completedUpdateStatusRequest.getFileSize())
          && p.getFilePathName()
          .equals(completedUpdateStatusRequest.getFilePathName())
          && p.getExportedRows()
          .equals(completedUpdateStatusRequest.getExportedRows())
          && p.getExpirationDate().equals(completedUpdateStatusRequest.getExpirationDate())
      ));
      Mockito.verify(scheduleExportFileExpirationActivityMock).scheduleExportFileExpiration(exportFileId,
        expectedDueDate
      );
    }
  }

  @Test
  void givenErrorCreatingFileWhenExportFileThenStatusError() {
    Long exportFileId = 1L;
    ExportFileTypeEnum exportFileType = ExportFileTypeEnum.PAID;
    UpdateStatusRequest processingUpdateStatusRequest = new UpdateStatusRequest(
      exportFileId,
      ExportFileStatus.REQUESTED, ExportFileStatus.PROCESSING, null, null,
      null,null,null, null);
    String errorMessage = "errorMessage";
    UpdateStatusRequest errorUpdateStatusRequest = new UpdateStatusRequest(
      exportFileId,
      ExportFileStatus.PROCESSING, ExportFileStatus.ERROR, null, null,
      null, null,errorMessage, null);

    Mockito.doNothing().when(updateExportFileStatusActivityMock).updateStatus(Mockito.any());
    Mockito.when(exportFileActivityMock.executeExport(exportFileId,exportFileType)).thenThrow(new RuntimeException(
      errorMessage));

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.exportFile(exportFileId,exportFileType);

      InOrder inOrder = Mockito.inOrder(updateExportFileStatusActivityMock, exportFileActivityMock);
      inOrder.verify(updateExportFileStatusActivityMock).updateStatus(Mockito.argThat(p->
        p.getExportFileId()
          .equals(processingUpdateStatusRequest.getExportFileId())
          && p.getOldStatus().equals(processingUpdateStatusRequest.getOldStatus())
          && p.getNewStatus().equals(processingUpdateStatusRequest.getNewStatus())
          && p.getFileName() == null
          && p.getFilePathName() == null
          && p.getFileSize() == null
          && p.getExportedRows() == null
          && p.getErrorDescription() == null
          && p.getExpirationDate() == null
      ));
      inOrder.verify(exportFileActivityMock).executeExport(exportFileId,exportFileType);
      inOrder.verify(updateExportFileStatusActivityMock).updateStatus(Mockito.argThat(p->
        p.getExportFileId()
          .equals(errorUpdateStatusRequest.getExportFileId())
          && p.getOldStatus().equals(errorUpdateStatusRequest.getOldStatus())
          && p.getNewStatus().equals(errorUpdateStatusRequest.getNewStatus())
          && p.getErrorDescription().equals(errorUpdateStatusRequest.getErrorDescription())
          && p.getFileName() == null
          && p.getFilePathName() == null
          && p.getFileSize() == null
          && p.getExportedRows() == null
          && p.getExpirationDate() == null
      ));
      Mockito.verifyNoInteractions(scheduleExportFileExpirationActivityMock);
    }
  }
}

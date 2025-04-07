package it.gov.pagopa.pu.workflow.service.exportfile;

import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile;
import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile.ExportFileTypeEnum;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.exportfile.ExportFileWFClient;
import it.gov.pagopa.pu.workflow.wf.exportfile.expiration.ExportFileExpirationHandlerWFClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExportFileServiceImplTest {

  @Mock
  private ExportFileWFClient exportFileWFClientMock;
  @Mock
  private ExportFileExpirationHandlerWFClient exportFileExpirationHandlerWFClientMock;

  private ExportFileService service;

  @BeforeEach
  void init(){
    service = new ExportFileServiceImpl(exportFileWFClientMock, exportFileExpirationHandlerWFClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(exportFileExpirationHandlerWFClientMock);
  }

  @Test
  void givenExportFileIdWhenExpireExportFileThenOk() {
    Long exportFileId = 1L;

    WorkflowCreatedDTO expectedResult = WorkflowCreatedDTO.builder()
      .workflowId("WFID")
      .build();

    Mockito.when(exportFileExpirationHandlerWFClientMock.exportFileExpirationHandler(Mockito.same(exportFileId)))
      .thenReturn("WFID");

    WorkflowCreatedDTO result = service.expireExportFile(exportFileId);

    Assertions.assertEquals(expectedResult, result);
  }

  @Test
  void givenExportFileIdWhenCreateExportFileThenOk() {
    Long exportFileId = 1L;

    WorkflowCreatedDTO expectedResult = WorkflowCreatedDTO.builder()
      .workflowId("WFID")
      .build();

    Mockito.when(exportFileWFClientMock.exportFile(Mockito.same(exportFileId), Mockito.any(
        ExportFile.ExportFileTypeEnum.class)))
      .thenReturn("WFID");

    WorkflowCreatedDTO result = service.exportFile(exportFileId, ExportFileTypeEnum.PAID);

    Assertions.assertEquals(expectedResult, result);
  }
}

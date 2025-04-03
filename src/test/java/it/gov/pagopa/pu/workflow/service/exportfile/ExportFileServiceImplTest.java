package it.gov.pagopa.pu.workflow.service.exportfile;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
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
  private ExportFileExpirationHandlerWFClient exportFileExpirationHandlerWFClientMock;

  private ExportFileService service;

  @BeforeEach
  void init(){
    service = new ExportFileServiceImpl(exportFileExpirationHandlerWFClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(exportFileExpirationHandlerWFClientMock);
  }

  @Test
  void givenExportFileIdWhenExportFileExpirationHandlerThenOk() {
    Long exportFileId = 1L;

    WorkflowCreatedDTO expectedResult = WorkflowCreatedDTO.builder()
      .workflowId("WFID")
      .build();

    Mockito.when(exportFileExpirationHandlerWFClientMock.exportFileExpirationHandler(Mockito.same(exportFileId)))
      .thenReturn("WFID");

    WorkflowCreatedDTO result = service.exportFileExpirationHandler(exportFileId);

    Assertions.assertEquals(expectedResult, result);
  }
}

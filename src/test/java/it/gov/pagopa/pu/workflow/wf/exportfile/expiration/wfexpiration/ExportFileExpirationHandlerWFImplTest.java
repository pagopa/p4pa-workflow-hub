package it.gov.pagopa.pu.workflow.wf.exportfile.expiration.wfexpiration;

import it.gov.pagopa.payhub.activities.activity.exportflow.ExportFileExpirationHandlerActivity;
import it.gov.pagopa.pu.workflow.wf.exportfile.expiration.config.ExportFileExpirationHandlerWFConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportFileExpirationHandlerWFImplTest {

  @Mock
  private ExportFileExpirationHandlerActivity exportFileExpirationHandlerActivityMock;

  private ExportFileExpirationHandlerWFImpl workflow;

  @BeforeEach
  void setUp() {
    ExportFileExpirationHandlerWFConfig configMock = mock(ExportFileExpirationHandlerWFConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);
    when(configMock.buildExportFileExpirationHandlerActivityStub()).thenReturn(exportFileExpirationHandlerActivityMock);

    when(applicationContextMock.getBean(ExportFileExpirationHandlerWFConfig.class)).thenReturn(configMock);

    workflow = new ExportFileExpirationHandlerWFImpl();
    workflow.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(exportFileExpirationHandlerActivityMock);
  }

  @Test
  void givenValidExportFileIdWhenCreateThenLogAndHandleExpiration() {
    Long exportFileId = 456L;

    workflow.exportFileExpirationHandler(exportFileId);

    verify(exportFileExpirationHandlerActivityMock).handleExpiration(exportFileId);
  }

  @Test
  void givenExceptionWhenCreateExportFileExpirationHandlerThenLogError() {
    Long exportFileId = 456L;
    doThrow(new RuntimeException("Test exception")).when(exportFileExpirationHandlerActivityMock).handleExpiration(exportFileId);

    assertThrows(RuntimeException.class, () -> workflow.exportFileExpirationHandler(exportFileId));

    verify(exportFileExpirationHandlerActivityMock).handleExpiration(exportFileId);
  }
}


package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.deletemassivenoticesfile;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.notice.DeleteMassiveNoticesFileActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.config.DeleteMassiveNoticesFileWFConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class DeleteMassiveNoticesFileWFTest {

  @Mock
  private DeleteMassiveNoticesFileActivity deleteMassiveNoticesFileActivityMock;

  private DeleteMassiveNoticesFileWFImpl wf;

  @BeforeEach
  void setUp() {
    DeleteMassiveNoticesFileWFConfig configMock = Mockito.mock(DeleteMassiveNoticesFileWFConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.doReturn(configMock)
      .when(applicationContextMock)
      .getBean(DeleteMassiveNoticesFileWFConfig.class);

    Mockito.when(configMock.buildDeleteMassiveNoticesFileActivityStub())
      .thenReturn(deleteMassiveNoticesFileActivityMock);

    wf = new DeleteMassiveNoticesFileWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(deleteMassiveNoticesFileActivityMock);
  }

  @Test
  void whenDeleteMassiveNoticesFileThenActivityIsCalled() {
    Long ingestionFlowFileId = 1L;

    wf.deleteMassiveNoticesFile(ingestionFlowFileId);

    Mockito.verify(deleteMassiveNoticesFileActivityMock)
      .deleteMassiveNoticesFile(ingestionFlowFileId);
  }
}

package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.wfmassivegeneration;

import it.gov.pagopa.payhub.activities.activity.ingestionflow.notice.FetchAndMergeNoticesActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.config.MassiveNoticesGenerationWFConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class MassiveNoticesGenerationWFImplTest {
  @Mock
  private FetchAndMergeNoticesActivity fetchAndMergeNoticesActivityMock;

  private MassiveNoticesGenerationWFImpl wf;

  @BeforeEach
  void setUp() {
    MassiveNoticesGenerationWFConfig massiveNoticesGenerationWFConfigMock = Mockito.mock(MassiveNoticesGenerationWFConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.doReturn(massiveNoticesGenerationWFConfigMock)
      .when(applicationContextMock)
      .getBean(MassiveNoticesGenerationWFConfig.class);

    Mockito.when(massiveNoticesGenerationWFConfigMock.buildFetchAndMergeNoticesActivityStub())
      .thenReturn(fetchAndMergeNoticesActivityMock);

    wf = new MassiveNoticesGenerationWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      fetchAndMergeNoticesActivityMock
    );
  }

  @Test
  void givenNonZeroResultWhenGenerateThenOk() {
    Long ingestionFlowFileId = 1L;

    Mockito.when(fetchAndMergeNoticesActivityMock.fetchAndMergeNotices(ingestionFlowFileId)).thenReturn(1);

    wf.generate(ingestionFlowFileId);

    Mockito.verify(fetchAndMergeNoticesActivityMock).fetchAndMergeNotices(ingestionFlowFileId);
  }
}

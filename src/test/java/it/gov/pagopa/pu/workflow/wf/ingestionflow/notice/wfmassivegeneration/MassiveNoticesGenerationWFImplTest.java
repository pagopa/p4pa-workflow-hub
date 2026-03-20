package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.wfmassivegeneration;

import io.temporal.workflow.Workflow;
import it.gov.pagopa.payhub.activities.activity.ingestionflow.notice.FetchAndMergeNoticesActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.activity.ScheduleMassiveNoticesFileDeletionWFActivity;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.config.MassiveNoticesGenerationWFConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.time.Duration;

@ExtendWith(MockitoExtension.class)
class MassiveNoticesGenerationWFImplTest {
  @Mock
  private FetchAndMergeNoticesActivity fetchAndMergeNoticesActivityMock;
  @Mock
  private ScheduleMassiveNoticesFileDeletionWFActivity scheduleMassiveNoticesFileDeletionWFActivityMock;

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
    Mockito.when(massiveNoticesGenerationWFConfigMock.buildScheduleMassiveNoticesFileDeletionWFActivityStub())
      .thenReturn(scheduleMassiveNoticesFileDeletionWFActivityMock);

    wf = new MassiveNoticesGenerationWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      fetchAndMergeNoticesActivityMock,
      scheduleMassiveNoticesFileDeletionWFActivityMock
    );
  }

  @Test
  void givenNonZeroResultWhenGenerateThenOk() {
    Long ingestionFlowFileId = 1L;

    Mockito.when(fetchAndMergeNoticesActivityMock.fetchAndMergeNotices(ingestionFlowFileId)).thenReturn(1);

    wf.generate(ingestionFlowFileId);

    Mockito.verify(fetchAndMergeNoticesActivityMock).fetchAndMergeNotices(ingestionFlowFileId);
    Mockito.verify(scheduleMassiveNoticesFileDeletionWFActivityMock).scheduleMassiveNoticesFileDeletionWF(ingestionFlowFileId);
  }

  @Test
  void givenZeroResultWhenGenerateThenSleepAndRetry() {
    Long ingestionFlowFileId = 1L;

    Mockito.when(fetchAndMergeNoticesActivityMock.fetchAndMergeNotices(ingestionFlowFileId))
      .thenReturn(0)
      .thenReturn(0)
      .thenReturn(1);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.generate(ingestionFlowFileId);

      Mockito.verify(fetchAndMergeNoticesActivityMock, Mockito.times(3)).fetchAndMergeNotices(ingestionFlowFileId);
      Mockito.verify(scheduleMassiveNoticesFileDeletionWFActivityMock).scheduleMassiveNoticesFileDeletionWF(ingestionFlowFileId);
    }
  }

  @Test
  void givenRepeatOneHundredTimesWhenGenerateThenContinueAsNew() {
    Long ingestionFlowFileId = 1L;

    Integer[] zeroResults = new Integer[99];
    Arrays.fill(zeroResults, 0);

    Mockito.when(fetchAndMergeNoticesActivityMock.fetchAndMergeNotices(ingestionFlowFileId))
      .thenReturn(0, zeroResults)
      .thenReturn(1);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(() -> Workflow.sleep(Mockito.any(Duration.class))).then(invocation -> null);

      wf.generate(ingestionFlowFileId);

      Mockito.verify(fetchAndMergeNoticesActivityMock, Mockito.times(101)).fetchAndMergeNotices(ingestionFlowFileId);
      workflowMock.verify(() -> Workflow.continueAsNew(ingestionFlowFileId), Mockito.times(1));
      Mockito.verify(scheduleMassiveNoticesFileDeletionWFActivityMock).scheduleMassiveNoticesFileDeletionWF(ingestionFlowFileId);
    }
  }
}

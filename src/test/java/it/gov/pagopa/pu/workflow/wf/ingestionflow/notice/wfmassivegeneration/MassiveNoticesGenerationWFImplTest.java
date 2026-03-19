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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.time.Duration;

@ExtendWith(MockitoExtension.class)
class MassiveNoticesGenerationWFImplTest {
  @Mock
  private FetchAndMergeNoticesActivity fetchAndMergeNoticesActivityMock;
  @Mock
  private ScheduleMassiveNoticesFileDeletionWFActivity scheduleMassiveNoticesFileDeletionWFActivity;

  private MassiveNoticesGenerationWFImpl wf;

  private static final long FIXED_TIME_MILLIS = Instant.parse("2024-01-01T10:00:00Z").toEpochMilli();
  private static final LocalDate EXPECTED_SCHEDULE_DATE = Instant.ofEpochMilli(FIXED_TIME_MILLIS)
    .atZone(ZoneId.systemDefault())
    .toLocalDate()
    .plusDays(100);

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
      .thenReturn(scheduleMassiveNoticesFileDeletionWFActivity);

    wf = new MassiveNoticesGenerationWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      fetchAndMergeNoticesActivityMock,
      scheduleMassiveNoticesFileDeletionWFActivity
    );
  }

  @Test
  void givenNonZeroResultWhenGenerateThenOk() {
    Long ingestionFlowFileId = 1L;

    Mockito.when(fetchAndMergeNoticesActivityMock.fetchAndMergeNotices(ingestionFlowFileId)).thenReturn(1);

    try (MockedStatic<Workflow> workflowMock = Mockito.mockStatic(Workflow.class)) {
      workflowMock.when(Workflow::currentTimeMillis).thenReturn(FIXED_TIME_MILLIS);

      wf.generate(ingestionFlowFileId);

      Mockito.verify(fetchAndMergeNoticesActivityMock).fetchAndMergeNotices(ingestionFlowFileId);
      Mockito.verify(scheduleMassiveNoticesFileDeletionWFActivity).scheduleMassiveNoticesFileDeletionWF(ingestionFlowFileId, EXPECTED_SCHEDULE_DATE);
    }
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
      workflowMock.when(Workflow::currentTimeMillis).thenReturn(FIXED_TIME_MILLIS);

      wf.generate(ingestionFlowFileId);

      Mockito.verify(fetchAndMergeNoticesActivityMock, Mockito.times(3)).fetchAndMergeNotices(ingestionFlowFileId);
      Mockito.verify(scheduleMassiveNoticesFileDeletionWFActivity).scheduleMassiveNoticesFileDeletionWF(ingestionFlowFileId, EXPECTED_SCHEDULE_DATE);
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
      workflowMock.when(Workflow::currentTimeMillis).thenReturn(FIXED_TIME_MILLIS);

      wf.generate(ingestionFlowFileId);

      Mockito.verify(fetchAndMergeNoticesActivityMock, Mockito.times(101)).fetchAndMergeNotices(ingestionFlowFileId);
      workflowMock.verify(() -> Workflow.continueAsNew(ingestionFlowFileId), Mockito.times(1));
      Mockito.verify(scheduleMassiveNoticesFileDeletionWFActivity).scheduleMassiveNoticesFileDeletionWF(ingestionFlowFileId, EXPECTED_SCHEDULE_DATE);
    }
  }
}

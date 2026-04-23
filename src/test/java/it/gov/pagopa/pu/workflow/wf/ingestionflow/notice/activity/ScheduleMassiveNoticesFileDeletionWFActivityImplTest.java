package it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.activity;

import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.DeleteMassiveNoticesFileWFClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

@ExtendWith(MockitoExtension.class)
class ScheduleMassiveNoticesFileDeletionWFActivityImplTest {
  @Mock
  private DeleteMassiveNoticesFileWFClient deleteMassiveNoticesFileWFClientMock;;

  private ScheduleMassiveNoticesFileDeletionWFActivity scheduleMassiveNoticesFileDeletionWFActivity;

  private static final int RETENTION_DAYS = 100;

  @BeforeEach
  void setUp() {
    scheduleMassiveNoticesFileDeletionWFActivity = new ScheduleMassiveNoticesFileDeletionWFActivityImpl(
      deleteMassiveNoticesFileWFClientMock,
      RETENTION_DAYS
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(deleteMassiveNoticesFileWFClientMock);
  }

  @Test
  void whenScheduleFileDeletionThenOk() {
    Long ingestionFlowFileId = 1L;
    Duration retentionDuration = Duration.ofDays(RETENTION_DAYS);

    scheduleMassiveNoticesFileDeletionWFActivity.scheduleMassiveNoticesFileDeletionWF(ingestionFlowFileId);

    Mockito.verify(deleteMassiveNoticesFileWFClientMock).scheduleMassiveNoticesFileDeletion(ingestionFlowFileId, retentionDuration);
  }
}

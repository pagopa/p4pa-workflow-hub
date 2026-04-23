package it.gov.pagopa.pu.workflow.wf.debtposition.iban.activity;

import it.gov.pagopa.pu.workflow.wf.debtposition.iban.MassiveDebtPositionWFClient;
import it.gov.pagopa.pu.workflow.wf.debtposition.iban.dto.MassiveIbanUpdateToSyncSignalDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

@ExtendWith(MockitoExtension.class)
class ScheduleToSyncMassiveIbanUpdateWFActivityImplTest {
  @Mock
  private MassiveDebtPositionWFClient massiveDebtPositionWFClientMock;

  private ScheduleToSyncMassiveIbanUpdateWFActivity scheduleToSyncMassiveIbanUpdateWFActivity;

  private static final int SCHEDULE_MINUTES = 5;

  @BeforeEach
  void setUp() {
    scheduleToSyncMassiveIbanUpdateWFActivity = new ScheduleToSyncMassiveIbanUpdateWFActivityImpl(massiveDebtPositionWFClientMock, SCHEDULE_MINUTES);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(massiveDebtPositionWFClientMock);
  }

  @Test
  void whenScheduleToSyncMassiveIbanUpdateWFThenOk() {
    // Given
    Long orgId = 1L;
    Long dptoId = 1L;
    String oldIban = "oldIban";
    String newIban = "newIban";
    String oldPostalIban = "oldPostalIban";
    String newPostalIban = "newPostalIban";

    MassiveIbanUpdateToSyncSignalDTO expectedSignalDTO = MassiveIbanUpdateToSyncSignalDTO.builder()
      .orgId(orgId)
      .dptoId(dptoId)
      .oldIban(oldIban)
      .newIban(newIban)
      .oldPostalIban(oldPostalIban)
      .newPostalIban(newPostalIban)
      .build();

    // When
    scheduleToSyncMassiveIbanUpdateWFActivity.scheduleToSyncMassiveIbanUpdateWF(orgId, dptoId, oldIban, newIban, oldPostalIban, newPostalIban);

    // Then
    Mockito.verify(massiveDebtPositionWFClientMock).scheduleMassiveIbanUpdateToSync(expectedSignalDTO, Duration.ofMinutes(SCHEDULE_MINUTES));
  }
}

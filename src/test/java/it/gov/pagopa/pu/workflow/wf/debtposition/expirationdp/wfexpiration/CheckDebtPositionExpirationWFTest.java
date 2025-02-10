package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration;

import it.gov.pagopa.payhub.activities.activity.debtposition.DebtPositionExpirationActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.activity.ScheduleCheckDpExpirationActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.config.CheckDebtPositionExpirationWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@ExtendWith(MockitoExtension.class)
class CheckDebtPositionExpirationWFTest {

  @Mock
  private DebtPositionExpirationActivity debtPositionExpirationActivityMock;
  @Mock
  private ScheduleCheckDpExpirationActivity scheduleCheckDpExpirationActivityMock;

  private CheckDebtPositionExpirationWFImpl wf;

  @BeforeEach
  void init() {
    CheckDebtPositionExpirationWfConfig configMock = Mockito.mock(CheckDebtPositionExpirationWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(configMock.buildDebtPositionExpirationActivityStub())
      .thenReturn(debtPositionExpirationActivityMock);

    Mockito.when(configMock.buildScheduleCheckDpExpirationActivityStub())
      .thenReturn(scheduleCheckDpExpirationActivityMock);

    Mockito.when(applicationContextMock.getBean(CheckDebtPositionExpirationWfConfig.class))
      .thenReturn(configMock);

    wf = new CheckDebtPositionExpirationWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      debtPositionExpirationActivityMock,
      scheduleCheckDpExpirationActivityMock);
  }

  @Test
  void givenCheckDpExpirationWithoutNextSchedulingThenSuccess() {
    // Given
    Long debtPositionId = 1L;

    Mockito.when(debtPositionExpirationActivityMock
      .checkAndUpdateInstallmentExpiration(debtPositionId)).thenReturn(null);

    // When
    wf.checkDpExpiration(debtPositionId);

    // Then
    Mockito.verify(debtPositionExpirationActivityMock).checkAndUpdateInstallmentExpiration(debtPositionId);
    Mockito.verify(scheduleCheckDpExpirationActivityMock, Mockito.times(0)).scheduleNextCheckDpExpiration(Mockito.any(), Mockito.any());
  }

  @Test
  void givenCheckDpExpirationWithNextSchedulingThenSuccess() {
    // Given
    Long debtPositionId = 1L;
    OffsetDateTime dateTime = OffsetDateTime.of(2025, 1, 1, 9, 12, 0, 0, ZoneOffset.UTC);

    Mockito.when(debtPositionExpirationActivityMock
      .checkAndUpdateInstallmentExpiration(debtPositionId)).thenReturn(dateTime);

    Mockito.doNothing().when(scheduleCheckDpExpirationActivityMock).scheduleNextCheckDpExpiration(debtPositionId, dateTime.plusDays(1));
    // When
    wf.checkDpExpiration(debtPositionId);

    // Then
    Mockito.verify(debtPositionExpirationActivityMock).checkAndUpdateInstallmentExpiration(debtPositionId);
    Mockito.verify(scheduleCheckDpExpirationActivityMock).scheduleNextCheckDpExpiration(debtPositionId, dateTime.plusDays(1));
  }
}

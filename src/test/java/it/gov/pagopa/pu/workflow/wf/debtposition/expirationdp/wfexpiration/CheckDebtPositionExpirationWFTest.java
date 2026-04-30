package it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.wfexpiration;

import it.gov.pagopa.payhub.activities.activity.debtposition.DebtPositionExpirationActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.expirationdp.config.CheckDebtPositionExpirationWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class CheckDebtPositionExpirationWFTest {

  @Mock
  private DebtPositionExpirationActivity debtPositionExpirationActivityMock;

  private CheckDebtPositionExpirationWFImpl wf;

  @BeforeEach
  void init() {
    CheckDebtPositionExpirationWfConfig configMock = Mockito.mock(CheckDebtPositionExpirationWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(configMock.buildDebtPositionExpirationActivityStub())
      .thenReturn(debtPositionExpirationActivityMock);

    Mockito.when(applicationContextMock.getBean(CheckDebtPositionExpirationWfConfig.class))
      .thenReturn(configMock);

    wf = new CheckDebtPositionExpirationWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      debtPositionExpirationActivityMock
    );
  }

  @Test
  void givenCheckDpExpirationWhenCheckDpExpirationThenInvokeActivity() {
    // Given
    Long debtPositionId = 1L;
    LocalDate date = LocalDate.of(2025, 1, 1);

    Mockito.when(debtPositionExpirationActivityMock
      .checkAndUpdateInstallmentExpiration(debtPositionId)).thenReturn(date);

    // When
    wf.checkDpExpiration(debtPositionId);

    // Then
    Mockito.verify(debtPositionExpirationActivityMock).checkAndUpdateInstallmentExpiration(debtPositionId);
  }
}

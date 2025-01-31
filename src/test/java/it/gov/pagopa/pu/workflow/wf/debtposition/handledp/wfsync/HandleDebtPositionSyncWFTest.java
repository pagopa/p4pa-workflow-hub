package it.gov.pagopa.pu.workflow.wf.debtposition.handledp.wfsync;

import it.gov.pagopa.payhub.activities.activity.debtposition.ionotification.SendDebtPositionIONotificationActivity;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.handledp.config.HandleDebtPositionWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;

@ExtendWith(MockitoExtension.class)
class HandleDebtPositionSyncWFTest {

  @Mock
  private SendDebtPositionIONotificationActivity sendDebtPositionIONotificationActivityMock;

  private HandleDebtPositionSyncWFImpl wf;

  @BeforeEach
  void init() {
    HandleDebtPositionWfConfig handleDebtPositionWfConfigMock = Mockito.mock(HandleDebtPositionWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(handleDebtPositionWfConfigMock.buildSendDebtPositionIONotificationActivityStub())
      .thenReturn(sendDebtPositionIONotificationActivityMock);

    Mockito.when(applicationContextMock.getBean(HandleDebtPositionWfConfig.class))
      .thenReturn(handleDebtPositionWfConfigMock);

    wf = new HandleDebtPositionSyncWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(sendDebtPositionIONotificationActivityMock);
  }

  @Test
  void givenHandleDPSyncThenSuccess() {
    // Given
    DebtPositionDTO debtPosition = buildDebtPositionDTO();

    Mockito.doNothing().when(sendDebtPositionIONotificationActivityMock)
      .sendMessage(debtPosition);

    // When
    wf.handleDPSync(debtPosition);

    // Then
    Mockito.verify(sendDebtPositionIONotificationActivityMock).sendMessage(debtPosition);
  }
}

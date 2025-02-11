package it.gov.pagopa.pu.workflow.wf.debtposition.handledp.wfsync;

import it.gov.pagopa.payhub.activities.activity.debtposition.ionotification.SendDebtPositionIONotificationActivity;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.handledp.config.HandleDebtPositionWfConfig;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync.SynchronizeSyncWFImpl;
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
class SynchronizeSyncWFTest {

  @Mock
  private SendDebtPositionIONotificationActivity sendDebtPositionIONotificationActivityMock;

  private SynchronizeSyncWFImpl wf;

  @BeforeEach
  void init() {
    HandleDebtPositionWfConfig handleDebtPositionWfConfigMock = Mockito.mock(HandleDebtPositionWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(handleDebtPositionWfConfigMock.buildSendDebtPositionIONotificationActivityStub())
      .thenReturn(sendDebtPositionIONotificationActivityMock);

    Mockito.when(applicationContextMock.getBean(HandleDebtPositionWfConfig.class))
      .thenReturn(handleDebtPositionWfConfigMock);

    wf = new SynchronizeSyncWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(sendDebtPositionIONotificationActivityMock);
  }

  @Test
  void givenSynchronizeDpSyncThenSuccess() {
    // Given
    DebtPositionDTO debtPosition = buildDebtPositionDTO();

    Mockito.doNothing().when(sendDebtPositionIONotificationActivityMock)
      .sendMessage(debtPosition);

    // When
    wf.synchronizeDpSync(debtPosition);

    // Then
    Mockito.verify(sendDebtPositionIONotificationActivityMock).sendMessage(debtPosition);
  }
}

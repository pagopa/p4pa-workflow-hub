package it.gov.pagopa.pu.workflow.wf.debtposition.sync.wf_sync;

import it.gov.pagopa.payhub.activities.activity.debtposition.ionotification.SendDebtPositionIONotificationActivity;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.event.payments.enums.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.PublishPaymentEventActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.config.SynchronizeDebtPositionWfConfig;
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
  @Mock
  private PublishPaymentEventActivity publishPaymentEventActivityMock;

  private SynchronizeSyncWFImpl wf;

  @BeforeEach
  void init() {
    SynchronizeDebtPositionWfConfig wfConfigMock = Mockito.mock(SynchronizeDebtPositionWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(wfConfigMock.buildSendDebtPositionIONotificationActivityStub())
      .thenReturn(sendDebtPositionIONotificationActivityMock);
    Mockito.when(wfConfigMock.buildPublishPaymentEventActivityStub())
        .thenReturn(publishPaymentEventActivityMock);

    Mockito.when(applicationContextMock.getBean(SynchronizeDebtPositionWfConfig.class))
      .thenReturn(wfConfigMock);

    wf = new SynchronizeSyncWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(sendDebtPositionIONotificationActivityMock);
  }

  @Test
  void givenSynchronizeDPSyncThenSuccess() {
    // Given
    DebtPositionDTO debtPosition = buildDebtPositionDTO();
    PaymentEventType paymentEventType = PaymentEventType.DP_CREATED;

    Mockito.doNothing().when(sendDebtPositionIONotificationActivityMock)
      .sendMessage(debtPosition);

    // When
    wf.synchronizeDPSync(debtPosition, paymentEventType);

    // Then
    Mockito.verify(sendDebtPositionIONotificationActivityMock).sendMessage(debtPosition);
    Mockito.verify(publishPaymentEventActivityMock).publish(debtPosition, paymentEventType, null);
  }
}

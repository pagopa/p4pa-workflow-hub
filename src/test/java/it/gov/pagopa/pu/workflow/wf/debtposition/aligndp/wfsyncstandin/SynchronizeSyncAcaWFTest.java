package it.gov.pagopa.pu.workflow.wf.debtposition.aligndp.wfsyncstandin;

import it.gov.pagopa.payhub.activities.activity.debtposition.FinalizeDebtPositionSyncStatusActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.aca.SynchronizeInstallmentAcaActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.ionotification.SendDebtPositionIONotificationActivity;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.workflow.event.payments.enums.PaymentEventType;
import it.gov.pagopa.pu.workflow.event.payments.producer.PaymentsProducerService;
import it.gov.pagopa.pu.workflow.wf.debtposition.aligndp.config.SynchronizeDebtPositionWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;

@ExtendWith(MockitoExtension.class)
class SynchronizeSyncAcaWFTest {

  @Mock
  private SynchronizeInstallmentAcaActivity synchronizeInstallmentAcaActivityMock;
  @Mock
  private FinalizeDebtPositionSyncStatusActivity finalizeDebtPositionSyncStatusActivityMock;
  @Mock
  private SendDebtPositionIONotificationActivity sendDebtPositionIONotificationActivityMock;
  @Mock
  private PaymentsProducerService paymentsProducerServiceMock;

  private SynchronizeSyncAcaWFImpl wf;

  @BeforeEach
  void init() {
    SynchronizeDebtPositionWfConfig createDebtPositionWfConfigMock = Mockito.mock(SynchronizeDebtPositionWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(createDebtPositionWfConfigMock.buildSynchronizeInstallmentAcaActivity())
      .thenReturn(synchronizeInstallmentAcaActivityMock);
    Mockito.when(createDebtPositionWfConfigMock.buildFinalizeDebtPositionSyncStatusActivityStub())
      .thenReturn(finalizeDebtPositionSyncStatusActivityMock);
    Mockito.when(createDebtPositionWfConfigMock.buildSendDebtPositionIONotificationActivityStub())
      .thenReturn(sendDebtPositionIONotificationActivityMock);

    Mockito.when(applicationContextMock.getBean(SynchronizeDebtPositionWfConfig.class))
      .thenReturn(createDebtPositionWfConfigMock);

    wf = new SynchronizeSyncAcaWFImpl(paymentsProducerServiceMock);
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      synchronizeInstallmentAcaActivityMock,
      finalizeDebtPositionSyncStatusActivityMock,
      sendDebtPositionIONotificationActivityMock);
  }

  @Test
  void givenSynchronizeDPSyncAcaThenOk() {
    // Given
    Long id = 1L;
    String iud = "iud";
    DebtPositionDTO debtPosition = buildDebtPositionDTO();

    Map<String, IupdSyncStatusUpdateDTO> syncStatusDTO = Map.of("iud", IupdSyncStatusUpdateDTO.builder()
      .newStatus(IupdSyncStatusUpdateDTO.NewStatusEnum.UNPAID)
      .iupdPagopa(null)
      .build());

    Mockito.when(finalizeDebtPositionSyncStatusActivityMock.finalizeDebtPositionSyncStatus(id, syncStatusDTO))
      .thenReturn(debtPosition);

    // When
    wf.synchronizeDPSyncAca(debtPosition);

    // Then
    Mockito.verify(synchronizeInstallmentAcaActivityMock).synchronizeInstallmentAca(debtPosition, iud);
    Mockito.verify(finalizeDebtPositionSyncStatusActivityMock).finalizeDebtPositionSyncStatus(id, syncStatusDTO);
    Mockito.verify(sendDebtPositionIONotificationActivityMock).sendMessage(debtPosition);
  }

  @Test
  void givenSynchronizeDPSyncAcaWhenExceptionThenPublishMessageToQueue() {
    // Given
    Long id = 1L;
    String iud = "iud";
    DebtPositionDTO debtPosition = buildDebtPositionDTO();

    Map<String, IupdSyncStatusUpdateDTO> syncStatusDTO = new HashMap<>();

    Mockito.doThrow(new IllegalArgumentException("Error"))
      .when(synchronizeInstallmentAcaActivityMock).synchronizeInstallmentAca(debtPosition, iud);

    Mockito.when(finalizeDebtPositionSyncStatusActivityMock.finalizeDebtPositionSyncStatus(id, syncStatusDTO))
      .thenReturn(debtPosition);

    // When
    wf.synchronizeDPSyncAca(debtPosition);

    // Then
    Mockito.verify(synchronizeInstallmentAcaActivityMock).synchronizeInstallmentAca(debtPosition, iud);
    Mockito.verify(paymentsProducerServiceMock).notifyPaymentsEvent(debtPosition, PaymentEventType.SYNC_ERROR, "Error");
    Mockito.verify(finalizeDebtPositionSyncStatusActivityMock).finalizeDebtPositionSyncStatus(id, syncStatusDTO);
    Mockito.verify(sendDebtPositionIONotificationActivityMock).sendMessage(debtPosition);
  }
}

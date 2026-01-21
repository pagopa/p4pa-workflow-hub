package it.gov.pagopa.pu.workflow.wf.debtposition.ionotification.wf_ionotification;

import it.gov.pagopa.payhub.activities.activity.debtposition.ionotification.IONotificationDebtPositionActivity;
import it.gov.pagopa.payhub.activities.dto.IONotificationMessage;
import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionIoNotificationDTO;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtposition.dto.generated.SyncCompleteDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.debtposition.ionotification.config.IoNotificationWfConfig;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.activity.PublishPaymentEventActivity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;

@ExtendWith(MockitoExtension.class)
class IoNotificationWFTest {

  @Mock
  private IONotificationDebtPositionActivity ioNotificationDebtPositionActivityMock;
  @Mock
  private PublishPaymentEventActivity publishPaymentEventActivityMock;

  private IoNotificationWFImpl wf;

  @BeforeEach
  void init() {
    IoNotificationWfConfig configMock = Mockito.mock(IoNotificationWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(configMock.buildIoNotificationDebtPositionActivityStub())
      .thenReturn(ioNotificationDebtPositionActivityMock);
    Mockito.when(configMock.buildPublishPaymentEventActivityStub())
      .thenReturn(publishPaymentEventActivityMock);

    Mockito.when(applicationContextMock.getBean(IoNotificationWfConfig.class))
      .thenReturn(configMock);

    wf = new IoNotificationWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      ioNotificationDebtPositionActivityMock,
      publishPaymentEventActivityMock
    );
  }

  @Test
  void givenIudSyncMapEmptyWhenSendSendIoNotificationThenNothingToNotify(){
    // Given
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    Map<String, SyncCompleteDTO> iudSyncCompleteDTOMap = new HashMap<>();
    GenericWfExecutionConfig.IONotificationBaseOpsMessages ioMessage =
      new GenericWfExecutionConfig.IONotificationBaseOpsMessages(new IONotificationMessage("subject", "message"), null, null);

    // When & Then
    Assertions.assertDoesNotThrow(() -> wf.sendIoNotification(debtPositionDTO, iudSyncCompleteDTOMap, ioMessage));
  }

  @Test
  void givenMessageToNotifyWhenSendSendIoNotificationThenPublishEvent(){
    // Given
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    Map<String, SyncCompleteDTO> iudSyncCompleteDTOMap = new HashMap<>();
    iudSyncCompleteDTOMap.put("SYNC_IUD", SyncCompleteDTO.builder().newStatus(InstallmentStatus.UNPAID).build());
    GenericWfExecutionConfig.IONotificationBaseOpsMessages ioMessage =
      new GenericWfExecutionConfig.IONotificationBaseOpsMessages(new IONotificationMessage("subject", "message"), null, null);

    DebtPositionIoNotificationDTO debtPositionIoNotificationDTO = DebtPositionIoNotificationDTO.builder()
      .debtPositionId(debtPositionDTO.getDebtPositionId())
      .debtPositionTypeOrgId(debtPositionDTO.getDebtPositionTypeOrgId())
      .organizationId(debtPositionDTO.getOrganizationId())
      .messages(List.of(new DebtPositionIoNotificationDTO.IoMessage()))
      .build();

    Mockito.when(ioNotificationDebtPositionActivityMock.sendIoNotification(debtPositionDTO, iudSyncCompleteDTOMap, ioMessage))
      .thenReturn(debtPositionIoNotificationDTO);

    Mockito.doNothing().when(publishPaymentEventActivityMock)
      .publishDebtPositionIoNotificationEvent(debtPositionIoNotificationDTO, new PaymentEventRequestDTO(PaymentEventType.IO_NOTIFIED, null));

    // When & Then
    Assertions.assertDoesNotThrow(() -> wf.sendIoNotification(debtPositionDTO, iudSyncCompleteDTOMap, ioMessage));
  }

  @Test
  void givenNoMessageToNotifyWhenSendSendIoNotificationThenPublishEvent(){
    // Given
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    Map<String, SyncCompleteDTO> iudSyncCompleteDTOMap = new HashMap<>();
    iudSyncCompleteDTOMap.put("SYNC_IUD", SyncCompleteDTO.builder().newStatus(InstallmentStatus.UNPAID).build());
    GenericWfExecutionConfig.IONotificationBaseOpsMessages ioMessage =
      new GenericWfExecutionConfig.IONotificationBaseOpsMessages(new IONotificationMessage("subject", "message"), null, null);

    Mockito.when(ioNotificationDebtPositionActivityMock.sendIoNotification(debtPositionDTO, iudSyncCompleteDTOMap, ioMessage))
      .thenReturn(null);

    // When & Then
    Assertions.assertDoesNotThrow(() -> wf.sendIoNotification(debtPositionDTO, iudSyncCompleteDTOMap, ioMessage));
  }
}

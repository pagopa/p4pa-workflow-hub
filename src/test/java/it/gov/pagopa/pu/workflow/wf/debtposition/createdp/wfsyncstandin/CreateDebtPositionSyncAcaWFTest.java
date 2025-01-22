package it.gov.pagopa.pu.workflow.wf.debtposition.createdp.wfsyncstandin;

import it.gov.pagopa.payhub.activities.activity.debtposition.FinalizeDebtPositionSyncStatusActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.aca.AcaStandInCreateDebtPositionActivity;
import it.gov.pagopa.payhub.activities.activity.debtposition.ionotification.SendDebtPositionIONotificationActivity;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.createdp.config.CreateDebtPositionWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Map;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CreateDebtPositionSyncAcaWFTest {

  @Mock
  private AcaStandInCreateDebtPositionActivity acaStandInCreateDebtPositionActivityMock;
  @Mock
  private FinalizeDebtPositionSyncStatusActivity finalizeDebtPositionSyncStatusActivityMock;
  @Mock
  private SendDebtPositionIONotificationActivity sendDebtPositionIONotificationActivityMock;

  private CreateDebtPositionSyncAcaWFImpl wf;

  @BeforeEach
  void init() {
    CreateDebtPositionWfConfig createDebtPositionWfConfigMock = Mockito.mock(CreateDebtPositionWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(createDebtPositionWfConfigMock.buildAcaStandInCreateDebtPositionActivityStub())
      .thenReturn(acaStandInCreateDebtPositionActivityMock);
    Mockito.when(createDebtPositionWfConfigMock.buildFinalizeDebtPositionSyncStatusActivityStub())
      .thenReturn(finalizeDebtPositionSyncStatusActivityMock);
    Mockito.when(createDebtPositionWfConfigMock.buildSendDebtPositionIONotificationActivityStub())
      .thenReturn(sendDebtPositionIONotificationActivityMock);

    Mockito.when(applicationContextMock.getBean(CreateDebtPositionWfConfig.class))
      .thenReturn(createDebtPositionWfConfigMock);

    wf = new CreateDebtPositionSyncAcaWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      acaStandInCreateDebtPositionActivityMock,
      finalizeDebtPositionSyncStatusActivityMock,
      sendDebtPositionIONotificationActivityMock);
  }

  @Test
  void givenCreateDPSyncAcaThenOk(){
    // Given
    Long id = 1L;
    IupdSyncStatusUpdateDTO.NewStatusEnum newStatus = IupdSyncStatusUpdateDTO.NewStatusEnum.UNPAID;
    String iupdPagoPa = "iupdPagoPa";
    DebtPositionDTO debtPosition = buildDebtPositionDTO();
    IupdSyncStatusUpdateDTO iupdSyncStatusUpdateDTO = IupdSyncStatusUpdateDTO.builder()
      .newStatus(newStatus)
      .iupdPagopa(iupdPagoPa)
      .build();
    Map<String, IupdSyncStatusUpdateDTO> syncStatusDTO = Map.of("iud", iupdSyncStatusUpdateDTO);

    Mockito.when(acaStandInCreateDebtPositionActivityMock.createAcaDebtPosition(debtPosition))
      .thenReturn(syncStatusDTO);

    debtPosition.setStatus(DebtPositionDTO.StatusEnum.UNPAID);
    debtPosition.getPaymentOptions().getFirst().setStatus(PaymentOptionDTO.StatusEnum.UNPAID);
    debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().setStatus(InstallmentDTO.StatusEnum.UNPAID);

    Mockito.when(finalizeDebtPositionSyncStatusActivityMock.finalizeDebtPositionSyncStatus(id, syncStatusDTO))
      .thenReturn(debtPosition);

    Mockito.doNothing().when(sendDebtPositionIONotificationActivityMock).sendMessage(debtPosition);

    // When
    wf.createDPSyncAca(debtPosition);

    // Then
    assertEquals(DebtPositionDTO.StatusEnum.UNPAID, debtPosition.getStatus());
    assertEquals(PaymentOptionDTO.StatusEnum.UNPAID, debtPosition.getPaymentOptions().getFirst().getStatus());
    assertEquals(InstallmentDTO.StatusEnum.UNPAID, debtPosition.getPaymentOptions().getFirst().getInstallments().getFirst().getStatus());

    Mockito.verify(acaStandInCreateDebtPositionActivityMock).createAcaDebtPosition(debtPosition);
    Mockito.verify(finalizeDebtPositionSyncStatusActivityMock).finalizeDebtPositionSyncStatus(id, syncStatusDTO);
    Mockito.verify(sendDebtPositionIONotificationActivityMock).sendMessage(debtPosition);
  }
}

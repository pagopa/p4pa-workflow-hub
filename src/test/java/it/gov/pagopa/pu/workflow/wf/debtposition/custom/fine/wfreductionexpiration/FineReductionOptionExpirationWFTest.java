package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfreductionexpiration;

import it.gov.pagopa.payhub.activities.activity.debtposition.custom.fine.DebtPositionFineReductionOptionExpirationActivity;
import it.gov.pagopa.payhub.activities.dto.IONotificationMessage;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.activity.InvokeSyncDebtPositionActivity;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.config.DebtPositionFineWfConfig;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.mapper.FineWfExecutionConfigMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class FineReductionOptionExpirationWFTest {

  @Mock
  private DebtPositionFineReductionOptionExpirationActivity debtPositionFineReductionOptionExpirationActivityMock;
  @Mock
  private InvokeSyncDebtPositionActivity invokeSyncDebtPositionActivityMock;

  private FineReductionOptionExpirationWFImpl wf;

  @BeforeEach
  void init() {
    DebtPositionFineWfConfig configMock = Mockito.mock(DebtPositionFineWfConfig.class);
    ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    Mockito.when(configMock.buildDebtPositionFineReductionOptionExpirationActivityStub())
      .thenReturn(debtPositionFineReductionOptionExpirationActivityMock);

    Mockito.when(configMock.buildInvokeSyncDebtPositionActivity())
      .thenReturn(invokeSyncDebtPositionActivityMock);

    Mockito.when(applicationContextMock.getBean(DebtPositionFineWfConfig.class))
      .thenReturn(configMock);

    wf = new FineReductionOptionExpirationWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      debtPositionFineReductionOptionExpirationActivityMock,
      invokeSyncDebtPositionActivityMock
    );
  }

  @Test
  void whenHandleFineReductionExpirationThenOk() {
    // Given
    Long debtPositionId = 1L;
    String workflowId = "workflowId";
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    PaymentEventRequestDTO paymentEventRequestDTO = new PaymentEventRequestDTO(PaymentEventType.IO_NOTIFIED, "description");
    GenericWfExecutionConfig wfExecutionConfig =
      new GenericWfExecutionConfig(new GenericWfExecutionConfig.IONotificationBaseOpsMessages(new IONotificationMessage("subject", "message"), null, null));

    FineWfExecutionConfig.IONotificationFineWfMessages fineWfMessages =
      new FineWfExecutionConfig.IONotificationFineWfMessages(null, new IONotificationMessage("subject", "message"));
    FineWfExecutionConfig fineWfExecutionConfig = new FineWfExecutionConfig();
    fineWfExecutionConfig.setIoMessages(fineWfMessages);

    Mockito.when(debtPositionFineReductionOptionExpirationActivityMock.handleFineReductionExpiration(debtPositionId))
      .thenReturn(debtPositionDTO);

    try (
      MockedStatic<FineWfExecutionConfigMapper> mapperMock = Mockito.mockStatic(FineWfExecutionConfigMapper.class)) {
      mapperMock.when(() -> FineWfExecutionConfigMapper.mapReductionExpired(fineWfExecutionConfig))
        .thenReturn(wfExecutionConfig);

      Mockito.when(invokeSyncDebtPositionActivityMock.synchronizeDPSync(debtPositionDTO, paymentEventRequestDTO, false, wfExecutionConfig))
        .thenReturn(workflowId);

      // When
      String result = wf.handleFineReductionExpiration(debtPositionId, paymentEventRequestDTO, false, fineWfExecutionConfig);

      // Then
      assertEquals(workflowId, result);
    }
  }

  @Test
  void givenDebtPositionNullWhenHandleFineReductionExpirationThenReturnNull() {
    // Given
    Long debtPositionId = 1L;
    PaymentEventRequestDTO paymentEventRequestDTO = new PaymentEventRequestDTO(PaymentEventType.IO_NOTIFIED, "description");
        FineWfExecutionConfig.IONotificationFineWfMessages fineWfMessages =
      new FineWfExecutionConfig.IONotificationFineWfMessages(null, new IONotificationMessage("subject", "message"));
    FineWfExecutionConfig fineWfExecutionConfig = new FineWfExecutionConfig();
    fineWfExecutionConfig.setIoMessages(fineWfMessages);

    Mockito.when(debtPositionFineReductionOptionExpirationActivityMock.handleFineReductionExpiration(debtPositionId))
      .thenReturn(null);

    // When
    String result = wf.handleFineReductionExpiration(debtPositionId, paymentEventRequestDTO, false, fineWfExecutionConfig);

    // Then
    assertNull(result);
  }
}

package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.wfreductionexpiration;

import it.gov.pagopa.payhub.activities.activity.debtposition.custom.fine.DebtPositionFineReductionOptionExpirationActivity;
import it.gov.pagopa.payhub.activities.dto.IONotificationMessage;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FineReductionOptionExpirationWFTest {

  @Mock
  private DebtPositionFineReductionOptionExpirationActivity debtPositionFineReductionOptionExpirationActivityMock;
  @Mock
  private InvokeSyncDebtPositionActivity invokeSyncDebtPositionActivityMock;

  private FineReductionOptionExpirationWFImpl wf;

  @BeforeEach
  void init() {
    DebtPositionFineWfConfig configMock = mock(DebtPositionFineWfConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);

    when(configMock.buildDebtPositionFineReductionOptionExpirationActivityStub())
      .thenReturn(debtPositionFineReductionOptionExpirationActivityMock);

    when(configMock.buildInvokeSyncDebtPositionActivityStub())
      .thenReturn(invokeSyncDebtPositionActivityMock);

    when(applicationContextMock.getBean(DebtPositionFineWfConfig.class))
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
  void whenExpireFineReductionThenOk() {
    // Given
    Long debtPositionId = 1L;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("workflowId", "runId");
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    GenericWfExecutionConfig wfExecutionConfig =
      new GenericWfExecutionConfig(new GenericWfExecutionConfig.IONotificationBaseOpsMessages(new IONotificationMessage("subject", "message"), null, null));
    FineWfExecutionConfig.IONotificationFineWfMessages fineWfMessages =
      new FineWfExecutionConfig.IONotificationFineWfMessages(null, new IONotificationMessage("subject", "message"));
    FineWfExecutionConfig fineWfExecutionConfig = new FineWfExecutionConfig();
    fineWfExecutionConfig.setIoMessages(fineWfMessages);

    when(debtPositionFineReductionOptionExpirationActivityMock.handleFineReductionExpiration(debtPositionId))
      .thenReturn(debtPositionDTO);

    try (
      MockedStatic<FineWfExecutionConfigMapper> mapperMock = Mockito.mockStatic(FineWfExecutionConfigMapper.class)) {
      mapperMock.when(() -> FineWfExecutionConfigMapper.mapReductionExpired(fineWfExecutionConfig, debtPositionDTO))
        .thenReturn(wfExecutionConfig);

      when(invokeSyncDebtPositionActivityMock.synchronizeDPSync(debtPositionDTO, null, false, wfExecutionConfig))
        .thenReturn(expectedResult);

      // When
      WorkflowCreatedDTO result = wf.expireFineReduction(debtPositionId, fineWfExecutionConfig);

      // Then
      assertEquals(expectedResult, result);
    }
  }

  @Test
  void givenDebtPositionNullWhenExpireFineReductionThenReturnNull() {
    // Given
    Long debtPositionId = 1L;
    FineWfExecutionConfig.IONotificationFineWfMessages fineWfMessages =
      new FineWfExecutionConfig.IONotificationFineWfMessages(null, new IONotificationMessage("subject", "message"));
    FineWfExecutionConfig fineWfExecutionConfig = new FineWfExecutionConfig();
    fineWfExecutionConfig.setIoMessages(fineWfMessages);

    when(debtPositionFineReductionOptionExpirationActivityMock.handleFineReductionExpiration(debtPositionId))
      .thenReturn(null);

    // When
    WorkflowCreatedDTO result = wf.expireFineReduction(debtPositionId, fineWfExecutionConfig);

    // Then
    assertNull(result);
  }
}

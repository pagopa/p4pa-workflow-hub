package it.gov.pagopa.pu.workflow.service.debtposition.custom.fine;

import it.gov.pagopa.payhub.activities.dto.IONotificationMessage;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.debtposition.sync.config.WfExecutionConfigHandlerService;
import it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.DebtPositionFineClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DebtPositionFineServiceTest {

  @Mock
  private DebtPositionFineClient debtPositionFineClientMock;
  @Mock
  private WfExecutionConfigHandlerService wfExecutionConfigHandlerServiceMock;

  private DebtPositionFineService service;

  @BeforeEach
  void init(){
    service = new DebtPositionFineServiceImpl(debtPositionFineClientMock, wfExecutionConfigHandlerServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(debtPositionFineClientMock, wfExecutionConfigHandlerServiceMock);
  }

  @Test
  void whenExpireFineReduction(){
    // Given
    Long debtPositionId = 1L;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("FineReductionOptionExpirationWF-1", "RUNID");
    FineWfExecutionConfig.IONotificationFineWfMessages fineWfMessages =
      new FineWfExecutionConfig.IONotificationFineWfMessages(null, new IONotificationMessage("subject", "message"));

    FineWfExecutionConfig wfExecutionConfig = new FineWfExecutionConfig();
    wfExecutionConfig.setIoMessages(fineWfMessages);

    Mockito.when(wfExecutionConfigHandlerServiceMock.findStoredExecutionConfig(debtPositionId, FineWfExecutionConfig.class))
        .thenReturn(wfExecutionConfig);

    Mockito.when(debtPositionFineClientMock.expireFineReduction(debtPositionId,  wfExecutionConfig))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = service.expireFineReduction(debtPositionId);

    // Then
    assertEquals(expectedResult, result);
  }
}

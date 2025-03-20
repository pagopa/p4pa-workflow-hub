package it.gov.pagopa.pu.workflow.service.debtposition.sync;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.service.debtposition.sync.complete.DebtPositionCompleteChangeSyncService;
import it.gov.pagopa.pu.workflow.service.debtposition.sync.config.WfExecutionConfigHandlerService;
import it.gov.pagopa.pu.workflow.service.debtposition.sync.partial.DebtPositionPartialChangeSyncService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DebtPositionSyncServiceTest {

  @Mock
  private WfExecutionConfigHandlerService wfConfigHandlerServiceMock;
  @Mock
  private DebtPositionPartialChangeSyncService partialChangeSyncServiceMock;
  @Mock
  private DebtPositionCompleteChangeSyncService completeChangeSyncServiceMock;

  private DebtPositionSyncService service;

  @BeforeEach
  void init(){
    service = new DebtPositionSyncService(
      wfConfigHandlerServiceMock,
      partialChangeSyncServiceMock,
      completeChangeSyncServiceMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      wfConfigHandlerServiceMock,
      partialChangeSyncServiceMock,
      completeChangeSyncServiceMock
    );
  }

  @Test
  void givenPartialChangeWhenInvokeWorkflowThenInvokePartialSync(){
    // Given
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    PaymentEventType paymentEventType = PaymentEventType.DP_CREATED;
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters(false, true, null);
    String accessToken = "accessToken";

    String expectedWfId = "WFID";
    Mockito.when(partialChangeSyncServiceMock.invokeWorkflow(Mockito.same(debtPositionDTO), Mockito.same(paymentEventType)))
      .thenReturn(expectedWfId);

    // When
    String wfId = service.invokeWorkflow(debtPositionDTO, paymentEventType, wfExecutionParameters, accessToken);

    // Then
    Assertions.assertSame(expectedWfId, wfId);

    Mockito.verify(wfConfigHandlerServiceMock)
      .persistAndConfigure(Mockito.same(debtPositionDTO), Mockito.same(wfExecutionParameters));
  }

  @Test
  void givenCompleteChangeWhenInvokeWorkflowThenInvokeCompleteSync(){
    // Given
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    PaymentEventType paymentEventType = PaymentEventType.DP_CREATED;
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters(false, false, null);
    String accessToken = "accessToken";

    String expectedWfId = "WFID";
    Mockito.when(completeChangeSyncServiceMock.invokeWorkflow(Mockito.same(debtPositionDTO), Mockito.same(paymentEventType), Mockito.same(wfExecutionParameters), Mockito.same(accessToken)))
      .thenReturn(expectedWfId);

    // When
    String wfId = service.invokeWorkflow(debtPositionDTO, paymentEventType, wfExecutionParameters, accessToken);

    // Then
    Assertions.assertSame(expectedWfId, wfId);

    Mockito.verify(wfConfigHandlerServiceMock)
      .persistAndConfigure(Mockito.same(debtPositionDTO), Mockito.same(wfExecutionParameters));
  }
}

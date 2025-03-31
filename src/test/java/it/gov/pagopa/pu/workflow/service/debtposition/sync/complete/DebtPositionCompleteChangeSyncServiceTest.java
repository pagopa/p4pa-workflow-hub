package it.gov.pagopa.pu.workflow.service.debtposition.sync.complete;

import it.gov.pagopa.payhub.activities.connector.workflowhub.dto.WfExecutionParameters;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.WfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.service.debtposition.sync.complete.custom.DebtPositionCustomWfSyncService;
import it.gov.pagopa.pu.workflow.service.debtposition.sync.complete.generic.DebtPositionGenericSyncService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DebtPositionCompleteChangeSyncServiceTest {

  @Mock
  private DebtPositionGenericSyncService genericWfSyncService;
  @Mock
  private DebtPositionCustomWfSyncService customWfSyncService;

  private DebtPositionCompleteChangeSyncService service;

  @BeforeEach
  void init(){
    service = new DebtPositionCompleteChangeSyncService(genericWfSyncService, customWfSyncService);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(genericWfSyncService, customWfSyncService);
  }

  @Test
  void givenNullExecutionConfigWhenInvokeWorkflowThenInvokeGenericSync(){
    whenInvokeWorkflowThenInvokeGenericSync(null);
  }
  @Test
  void givenGenericWfExecutionConfigWhenInvokeWorkflowThenInvokeGenericSync(){
    whenInvokeWorkflowThenInvokeGenericSync(new GenericWfExecutionConfig());
  }
  void whenInvokeWorkflowThenInvokeGenericSync(GenericWfExecutionConfig wfExecutionConfig){
    // Given
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO(PaymentEventType.DP_CREATED, "EVENTDESCRIPTION");
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters(false, false, wfExecutionConfig);
    String accessToken = "accessToken";

    String expectedWfId = "WFID";
    Mockito.when(genericWfSyncService.invokeWorkflow(Mockito.same(debtPositionDTO), Mockito.same(paymentEventRequest), Mockito.eq(false), Mockito.same(wfExecutionConfig), Mockito.same(accessToken)))
      .thenReturn(expectedWfId);

    // When
    String wfId = service.invokeWorkflow(debtPositionDTO, paymentEventRequest, wfExecutionParameters, accessToken);

    // Then
    Assertions.assertSame(expectedWfId, wfId);
  }

  @Test
  void givenOtherWfExecutionConfigWhenInvokeWorkflowThenInvokeCustomWfSync(){
    // Given
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO(PaymentEventType.DP_CREATED, "EVENTDESCRIPTION");
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters(false, false, Mockito.mock(WfExecutionConfig.class));
    String accessToken = "accessToken";

    String expectedWfId = "WFID";
    Mockito.when(customWfSyncService.invokeWorkflow(Mockito.same(debtPositionDTO), Mockito.same(paymentEventRequest), Mockito.same(wfExecutionParameters)))
      .thenReturn(expectedWfId);

    // When
    String wfId = service.invokeWorkflow(debtPositionDTO, paymentEventRequest, wfExecutionParameters, accessToken);

    // Then
    Assertions.assertSame(expectedWfId, wfId);
  }
}

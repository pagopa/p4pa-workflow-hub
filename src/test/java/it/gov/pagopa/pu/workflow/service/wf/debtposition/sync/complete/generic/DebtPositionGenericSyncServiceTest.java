package it.gov.pagopa.pu.workflow.service.wf.debtposition.sync.complete.generic;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.organization.dto.generated.PagoPaInteractionModel;
import it.gov.pagopa.pu.workflow.dto.PaymentEventRequestDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.SynchronizeDebtPositionWfClient;
import org.apache.commons.lang3.function.TriFunction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DebtPositionGenericSyncServiceTest {

  @Mock
  private PagoPASyncInteractionModelRetrieverService interactionModelRetrieverServiceMock;
  @Mock
  private SynchronizeDebtPositionWfClient wfClientMock;

  private DebtPositionGenericSyncService service;

  @BeforeEach
  void init() {
    service = new DebtPositionGenericSyncService(interactionModelRetrieverServiceMock, wfClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(interactionModelRetrieverServiceMock, wfClientMock);
  }

  @Test
  void givenNotPagoPaPaymentWhenInvokeWorkflowThenInvokeWfClient() {
    // Given
    String accessToken = "ACCESSTOKEN";
    DebtPositionDTO debtPosition = new DebtPositionDTO();
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO(PaymentEventType.DP_CREATED, "EVENTDESCRIPTION");
    GenericWfExecutionConfig wfExecutionConfig = new GenericWfExecutionConfig();

    debtPosition.setFlagPagoPaPayment(false);
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("WFID", "RUNID");
    Mockito.when(wfClientMock.synchronizeNoPagoPADP(Mockito.same(debtPosition), Mockito.same(paymentEventRequest), Mockito.same(wfExecutionConfig)))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = service.invokeWorkflow(debtPosition, paymentEventRequest, false, wfExecutionConfig, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenSYNCWhenInvokeWorkflowThenInvokeWfClient() {
    testInvokeWorkflowThenInvokeWfClient(PagoPaInteractionModel.SYNC, false, wfClientMock::synchronizeDPSync);
  }
  @Test
  void givenSYNC_ACAWhenInvokeWorkflowThenInvokeWfClient() {
    testInvokeWorkflowThenInvokeWfClient(PagoPaInteractionModel.SYNC_ACA, false, wfClientMock::synchronizeDPSyncAca);
  }
  @Test
  void givenSYNC_ACA_GPDPRELOADWhenInvokeWorkflowThenInvokeWfClient() {
    testInvokeWorkflowThenInvokeWfClient(PagoPaInteractionModel.SYNC_ACA_GPDPRELOAD, false, wfClientMock::synchronizeDPSyncAcaGpdPreLoad);
  }
  @Test
  void givenSYNC_GPDPRELOADWhenInvokeWorkflowThenInvokeWfClient() {
    testInvokeWorkflowThenInvokeWfClient(PagoPaInteractionModel.SYNC_GPDPRELOAD, false, wfClientMock::synchronizeDPSyncGpdPreLoad);
  }
  @Test
  void givenASYNC_GPDWhenInvokeWorkflowThenInvokeWfClient() {
    testInvokeWorkflowThenInvokeWfClient(PagoPaInteractionModel.ASYNC_GPD, false, wfClientMock::synchronizeDPAsyncGpd);
  }
  @Test
  void givenMassiveASYNC_GPDWhenInvokeWorkflowThenInvokeWfClient() {
    testInvokeWorkflowThenInvokeWfClient(PagoPaInteractionModel .ASYNC_GPD, true, null);
  }

  private void testInvokeWorkflowThenInvokeWfClient(PagoPaInteractionModel  interactionModel, boolean massive, TriFunction<DebtPositionDTO, PaymentEventRequestDTO, GenericWfExecutionConfig, WorkflowCreatedDTO> expectedWfClientInvoke) {
    // Given
    String accessToken = "ACCESSTOKEN";
    long organizationId = 1L;
    DebtPositionDTO debtPosition = new DebtPositionDTO();
    debtPosition.setOrganizationId(organizationId);
    PaymentEventRequestDTO paymentEventRequest = new PaymentEventRequestDTO(PaymentEventType.DP_CREATED, "EVENTDESCRIPTION");
    GenericWfExecutionConfig wfExecutionConfig = new GenericWfExecutionConfig();

    Mockito.when(interactionModelRetrieverServiceMock.retrieveInteractionModel(organizationId, accessToken))
      .thenReturn(interactionModel);

    debtPosition.setFlagPagoPaPayment(true);
    WorkflowCreatedDTO expectedResult = null;
    if (expectedWfClientInvoke != null) {
      expectedResult = new WorkflowCreatedDTO("WFID", "RUNID");
      Mockito.when(expectedWfClientInvoke.apply(Mockito.same(debtPosition), Mockito.same(paymentEventRequest), Mockito.same(wfExecutionConfig)))
        .thenReturn(expectedResult);
    }

    // When
    WorkflowCreatedDTO result = service.invokeWorkflow(debtPosition, paymentEventRequest, massive, wfExecutionConfig, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }
}

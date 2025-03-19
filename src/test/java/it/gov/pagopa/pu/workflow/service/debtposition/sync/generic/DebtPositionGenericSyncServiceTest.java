package it.gov.pagopa.pu.workflow.service.debtposition.sync.generic;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflow.wf.debtposition.sync.SynchronizeDebtPositionWfClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.BiFunction;

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
    PaymentEventType paymentEventType = PaymentEventType.DP_CREATED;
    Boolean massive = Boolean.TRUE;

    debtPosition.setFlagPagoPaPayment(false);
    String expectedWorkflowId = "WFID";
    Mockito.when(wfClientMock.synchronizeNoPagoPADP(Mockito.same(debtPosition), Mockito.same(paymentEventType)))
      .thenReturn(expectedWorkflowId);

    // When
    String result = service.invokeWorkflow(debtPosition, paymentEventType, massive, accessToken);

    // Then
    Assertions.assertSame(expectedWorkflowId, result);
  }

  @Test
  void givenSYNCWhenInvokeWorkflowThenInvokeWfClient() {
    testInvokeWorkflowThenInvokeWfClient(Broker.PagoPaInteractionModelEnum.SYNC, false, wfClientMock::synchronizeDPSync);
  }
  @Test
  void givenSYNC_ACAWhenInvokeWorkflowThenInvokeWfClient() {
    testInvokeWorkflowThenInvokeWfClient(Broker.PagoPaInteractionModelEnum.SYNC_ACA, false, wfClientMock::synchronizeDPSyncAca);
  }
  @Test
  void givenSYNC_ACA_GPDPRELOADWhenInvokeWorkflowThenInvokeWfClient() {
    testInvokeWorkflowThenInvokeWfClient(Broker.PagoPaInteractionModelEnum.SYNC_ACA_GPDPRELOAD, false, wfClientMock::synchronizeDPSyncAcaGpdPreLoad);
  }
  @Test
  void givenSYNC_GPDPRELOADWhenInvokeWorkflowThenInvokeWfClient() {
    testInvokeWorkflowThenInvokeWfClient(Broker.PagoPaInteractionModelEnum.SYNC_GPDPRELOAD, false, wfClientMock::synchronizeDPSyncGpdPreLoad);
  }
  @Test
  void givenASYNC_GPDWhenInvokeWorkflowThenInvokeWfClient() {
    testInvokeWorkflowThenInvokeWfClient(Broker.PagoPaInteractionModelEnum.ASYNC_GPD, false, wfClientMock::synchronizeDPAsyncGpd);
  }
  @Test
  void givenMassiveASYNC_GPDWhenInvokeWorkflowThenInvokeWfClient() {
    testInvokeWorkflowThenInvokeWfClient(Broker.PagoPaInteractionModelEnum.ASYNC_GPD, true, null);
  }

  private void testInvokeWorkflowThenInvokeWfClient(Broker.PagoPaInteractionModelEnum interactionModel, boolean massive, BiFunction<DebtPositionDTO, PaymentEventType, String> expectedWfClientInvoke) {
    // Given
    String accessToken = "ACCESSTOKEN";
    long organizationId = 1L;
    DebtPositionDTO debtPosition = new DebtPositionDTO();
    debtPosition.setOrganizationId(organizationId);
    PaymentEventType paymentEventType = PaymentEventType.DP_CREATED;

    Mockito.when(interactionModelRetrieverServiceMock.retrieveInteractionModel(organizationId, accessToken))
      .thenReturn(interactionModel);

    debtPosition.setFlagPagoPaPayment(true);
    String expectedWorkflowId = null;
    if (expectedWfClientInvoke != null) {
      expectedWorkflowId = "WFID";
      Mockito.when(expectedWfClientInvoke.apply(Mockito.same(debtPosition), Mockito.same(paymentEventType)))
        .thenReturn(expectedWorkflowId);
    }

    // When
    String result = service.invokeWorkflow(debtPosition, paymentEventType, massive, accessToken);

    // Then
    Assertions.assertSame(expectedWorkflowId, result);
  }
}

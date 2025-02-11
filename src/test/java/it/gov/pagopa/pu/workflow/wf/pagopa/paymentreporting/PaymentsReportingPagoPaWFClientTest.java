package it.gov.pagopa.pu.workflow.wf.pagopa.paymentreporting;

import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentreporting.wffetch.PaymentsReportingPagoPaWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentreporting.wffetch.PaymentsReportingPagoPaWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingPagoPaWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private PaymentsReportingPagoPaWF wfMock;

  private PaymentsReportingPagoPaWFClient client;

  @BeforeEach
  void setUp() {
    client = new PaymentsReportingPagoPaWFClient(workflowServiceMock);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void testRetrieve() {
    // Given
    long organizationId = 1L;
    String expectedWorkflowId = "PaymentsReportingPagoPaWF-1";

    try (MockedStatic<Utilities> utilitiesMockedStatic = mockStatic(Utilities.class)) {
      utilitiesMockedStatic
        .when(() -> Utilities.generateWorkflowId(organizationId, PaymentsReportingPagoPaWFImpl.TASK_QUEUE))
        .thenReturn(expectedWorkflowId);

      Mockito.when(workflowServiceMock.buildWorkflowStub(PaymentsReportingPagoPaWF.class, PaymentsReportingPagoPaWFImpl.TASK_QUEUE, expectedWorkflowId))
        .thenReturn(wfMock);

      // When
      String workflowId = client.retrieve(organizationId);

      // Then
      assertEquals(expectedWorkflowId, workflowId);
      verify(wfMock).retrieve(organizationId);
    }
  }
}

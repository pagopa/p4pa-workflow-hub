package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting;

import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wforganizationfetch.PaymentsReportingPagoPaOrganizationFetchWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wforganizationfetch.PaymentsReportingPagoPaOrganizationFetchWFImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingPagoPaOrganizationFetchWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private PaymentsReportingPagoPaOrganizationFetchWF wfMock;

  private OrganizationPaymentsReportingPagoPaFetchWFClient client;

  @BeforeEach
  void setUp() {
    client = new OrganizationPaymentsReportingPagoPaFetchWFClient(workflowServiceMock);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(workflowServiceMock);
  }

  @Test
  void testRetrieve() {
    // Given
    long organizationId = 1L;
    String taskQueue = PaymentsReportingPagoPaOrganizationFetchWFImpl.TASK_QUEUE_ORGANIZATION_PAYMENTS_REPORTING_PAGOPA_FETCH;
    String expectedWorkflowId = "PaymentsReportingPagoPaOrganizationFetchWF-1";

    Mockito.when(workflowServiceMock.buildWorkflowStub(PaymentsReportingPagoPaOrganizationFetchWF.class, taskQueue, expectedWorkflowId))
      .thenReturn(wfMock);

    // When
    String workflowId = client.retrieve(organizationId);

    // Then
    assertEquals(expectedWorkflowId, workflowId);
    verify(wfMock).retrieve(organizationId);
  }
}

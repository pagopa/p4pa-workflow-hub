package it.gov.pagopa.pu.workflow.wf.paymentsreporting.pagopa;

import it.gov.pagopa.pu.workflow.service.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import it.gov.pagopa.pu.workflow.wf.paymentsreporting.pagopa.wffetch.OrganizationPaymentsReportingPagoPaFetchWF;
import it.gov.pagopa.pu.workflow.wf.paymentsreporting.pagopa.wffetch.OrganizationPaymentsReportingPagoPaFetchWFImpl;
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
class OrganizationPaymentsReportingPagoPaFetchWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private OrganizationPaymentsReportingPagoPaFetchWF wfMock;

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
    String expectedWorkflowId = "OrganizationPaymentsReportingPagoPaFetchWF-1";

    try (MockedStatic<Utilities> utilitiesMockedStatic = mockStatic(Utilities.class)) {
      utilitiesMockedStatic
        .when(() -> Utilities.generateWorkflowId(organizationId, OrganizationPaymentsReportingPagoPaFetchWFImpl.TASK_QUEUE_ORGANIZATION_PAYMENTS_REPORTING_PAGOPA_FETCH))
        .thenReturn(expectedWorkflowId);

      Mockito.when(workflowServiceMock.buildWorkflowStub(OrganizationPaymentsReportingPagoPaFetchWF.class, OrganizationPaymentsReportingPagoPaFetchWFImpl.TASK_QUEUE_ORGANIZATION_PAYMENTS_REPORTING_PAGOPA_FETCH, expectedWorkflowId))
        .thenReturn(wfMock);

      // When
      String workflowId = client.retrieve(organizationId);

      // Then
      assertEquals(expectedWorkflowId, workflowId);
      verify(wfMock).retrieve(organizationId);
    }
  }
}

package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
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
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private PaymentsReportingPagoPaOrganizationFetchWF wfMock;

  private OrganizationPaymentsReportingPagoPaFetchWFClient client;

  @BeforeEach
  void setUp() {
    client = new OrganizationPaymentsReportingPagoPaFetchWFClient(workflowServiceMock, workflowClientServiceMock);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock);
  }

  @Test
  void testRetrieve() {
    // Given
    long organizationId = 1L;
    String taskQueue = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("PaymentsReportingPagoPaOrganizationFetchWF-1", "RUNID");

    Mockito.when(workflowServiceMock.buildWorkflowStubToStartNew(PaymentsReportingPagoPaOrganizationFetchWF.class, taskQueue, expectedResult.getWorkflowId()))
      .thenReturn(wfMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, organizationId);

    // When
    WorkflowCreatedDTO result = client.retrieve(organizationId);

    // Then
    assertEquals(expectedResult, result);
    verify(wfMock).retrieve(organizationId);

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, PaymentsReportingPagoPaOrganizationFetchWFImpl.class);
  }
}

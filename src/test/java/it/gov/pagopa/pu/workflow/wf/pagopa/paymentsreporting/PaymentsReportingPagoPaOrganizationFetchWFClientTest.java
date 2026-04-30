package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting;

import it.gov.pagopa.payhub.activities.connector.auth.AuthnService;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflow.connector.organization.service.BrokerService;
import it.gov.pagopa.pu.workflow.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowClientService;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowService;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.utils.TemporalTestUtils;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wforganizationfetch.PaymentsReportingPagoPaOrganizationFetchWF;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wforganizationfetch.PaymentsReportingPagoPaOrganizationFetchWFImpl;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingPagoPaOrganizationFetchWFClientTest {

  @Mock
  private WorkflowService workflowServiceMock;
  @Mock
  private WorkflowClientService workflowClientServiceMock;
  @Mock
  private PaymentsReportingPagoPaOrganizationFetchWF wfMock;
  @Mock
  private OrganizationService organizationServiceMock;
  @Mock
  private BrokerService brokerServiceMock;
  @Mock
  private AuthnService authnServiceMock;

  private OrganizationPaymentsReportingPagoPaFetchWFClient client;

  @BeforeEach
  void setUp() {
    client = new OrganizationPaymentsReportingPagoPaFetchWFClient(workflowServiceMock, workflowClientServiceMock, organizationServiceMock, brokerServiceMock, authnServiceMock);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(workflowServiceMock, workflowClientServiceMock, organizationServiceMock, brokerServiceMock, authnServiceMock);
  }

  @Test
  void testRetrieveWhenFlagsPaymentsReportingTrueThenShouldStartWorkflow() {
    // Given
    long organizationId = 1L;
    String token = "TOKEN";
    String taskQueue = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY;

    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("PaymentsReportingPagoPaOrganizationFetchWF-1", "RUNID");

    when(authnServiceMock.getAccessToken()).thenReturn(token);

    Organization org = new Organization()
      .organizationId(organizationId)
      .flagPaymentsReporting(true);

    when(organizationServiceMock.getOrganizationById(organizationId, token)).thenReturn(org);

    Broker broker = new Broker()
      .brokerId(10L)
      .flagPaymentsReporting(true);

    when(brokerServiceMock.findByBrokeredOrganizationId(organizationId, token))
      .thenReturn(Optional.of(broker));

    when(workflowServiceMock.buildWorkflowStubToStartNew(
      PaymentsReportingPagoPaOrganizationFetchWF.class,
      taskQueue,
      expectedResult.getWorkflowId()
    )).thenReturn(wfMock);

    TemporalTestUtils.configureWorkflowClientServiceMock(workflowClientServiceMock, expectedResult, organizationId);

    // When
    WorkflowCreatedDTO result = client.retrieve(organizationId);

    // Then
    assertEquals(expectedResult, result);
    verify(workflowClientServiceMock, times(1)).start(any(), eq(organizationId));

    TemporalTestUtils.verifyWorkflowTaskQueueConfiguration(taskQueue, PaymentsReportingPagoPaOrganizationFetchWFImpl.class);
  }

  @Test
  void testRetrieveWhenOrganizationFlagPaymentsReportingFalseThenShouldThrowValidationException() {
    // Given
    long organizationId = 1L;
    String token = "TOKEN";
    when(authnServiceMock.getAccessToken()).thenReturn(token);

    Organization org = new Organization()
      .organizationId(organizationId)
      .flagPaymentsReporting(false);

    when(organizationServiceMock.getOrganizationById(organizationId, token)).thenReturn(org);

    // When + Then
    assertThrows(ValidationException.class, () -> client.retrieve(organizationId));
    verify(workflowClientServiceMock, never()).start(any(), any());
  }

  @Test
  void testRetrieveWhenNoBrokerFoundThenShouldThrowValidationException() {
    // Given
    long organizationId = 1L;
    String token = "TOKEN";
    when(authnServiceMock.getAccessToken()).thenReturn(token);

    Organization org = new Organization()
      .organizationId(organizationId)
      .flagPaymentsReporting(true);

    when(organizationServiceMock.getOrganizationById(organizationId, token)).thenReturn(org);

    when(brokerServiceMock.findByBrokeredOrganizationId(organizationId, token))
      .thenReturn(Optional.empty());

    // When + Then
    assertThrows(ValidationException.class, () -> client.retrieve(organizationId));
    verify(workflowClientServiceMock, never()).start(any(), any());
  }

  @Test
  void testRetrieveWhenBrokerFlagFalseThenShouldThrowValidationException() {
    // Given
    long organizationId = 1L;
    String token = "TOKEN";
    when(authnServiceMock.getAccessToken()).thenReturn(token);

    Organization org = new Organization()
      .organizationId(organizationId)
      .flagPaymentsReporting(true);

    when(organizationServiceMock.getOrganizationById(organizationId, token)).thenReturn(org);

    Broker broker = new Broker()
      .brokerId(10L)
      .flagPaymentsReporting(false);

    when(brokerServiceMock.findByBrokeredOrganizationId(organizationId, token))
      .thenReturn(Optional.of(broker));

    // When + Then
    assertThrows(ValidationException.class, () -> client.retrieve(organizationId));
    verify(workflowClientServiceMock, never()).start(any(), any());
  }
}

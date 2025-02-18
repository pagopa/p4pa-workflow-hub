package it.gov.pagopa.pu.workflow.wf.paymentsreporting.broker.activity;

import it.gov.pagopa.pu.workflow.wf.paymentsreporting.pagopa.OrganizationPaymentsReportingPagoPaFetchWFClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChainOrganizationsBrokered2OrganizationPaymentsReportingActivityTest {
  @Mock
  private OrganizationPaymentsReportingPagoPaFetchWFClient mockClient;

  private ChainOrganizationsBrokered2OrganizationPaymentsReportingActivityImpl activity;

  @BeforeEach
  void setUp() {
    activity = new ChainOrganizationsBrokered2OrganizationPaymentsReportingActivityImpl(mockClient);
  }

  @Test
  void chainWithValidOrganizationId() {
    // Given
    Long organizationId = 1L;
    String workflowId = "workflowId";

    when(mockClient.retrieve(organizationId)).thenReturn(workflowId);

    // When Then
    assertDoesNotThrow(() -> activity.chain(organizationId));
    verify(mockClient, times(1)).retrieve(organizationId);
  }
}

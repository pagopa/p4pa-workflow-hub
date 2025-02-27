package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wfbrokersfetch;

import it.gov.pagopa.payhub.activities.activity.organization.BrokersRetrieverActivity;
import it.gov.pagopa.payhub.activities.activity.organization.OrganizationBrokeredRetrieverActivity;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.OrganizationPaymentsReportingPagoPaFetchWFClient;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.config.BrokersPaymentsReportingPagoPaFetchWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentsReportingPagoPaBrokersFetchWFTest {

  @Mock
  private BrokersRetrieverActivity brokersRetrieverActivityMock;

  @Mock
  private OrganizationBrokeredRetrieverActivity organizationBrokeredRetrieverActivityMock;

  @Mock
  private OrganizationPaymentsReportingPagoPaFetchWFClient organizationPaymentsReportingPagoPaFetchWFClientMock;

  private PaymentsReportingPagoPaBrokersFetchWFImpl workflow;

  @BeforeEach
  void setUp() {
    BrokersPaymentsReportingPagoPaFetchWfConfig configMock = mock(BrokersPaymentsReportingPagoPaFetchWfConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);
    when(configMock.buildBrokersRetrieverActivityStub()).thenReturn(brokersRetrieverActivityMock);
    when(configMock.buildOrganizationBrokeredRetrieverActivityStub()).thenReturn(organizationBrokeredRetrieverActivityMock);
    when(applicationContextMock.getBean(OrganizationPaymentsReportingPagoPaFetchWFClient.class)).thenReturn(organizationPaymentsReportingPagoPaFetchWFClientMock);
    when(applicationContextMock.getBean(BrokersPaymentsReportingPagoPaFetchWfConfig.class)).thenReturn(configMock);

    workflow = new PaymentsReportingPagoPaBrokersFetchWFImpl();
    workflow.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(brokersRetrieverActivityMock, organizationBrokeredRetrieverActivityMock, organizationPaymentsReportingPagoPaFetchWFClientMock);
  }

  @Test
  void retrieveWithValidData() {
    Broker broker = new Broker().brokerId(1L);
    Organization organization = new Organization().organizationId(1L);

    when(brokersRetrieverActivityMock.fetchAll()).thenReturn(List.of(broker));
    when(organizationBrokeredRetrieverActivityMock.retrieve(1L)).thenReturn(List.of(organization));

    when(brokersRetrieverActivityMock.fetchAll()).thenReturn(List.of(broker));
    when(organizationBrokeredRetrieverActivityMock.retrieve(1L)).thenReturn(List.of(organization));
    when(organizationPaymentsReportingPagoPaFetchWFClientMock.retrieveAsyncStart(organization.getOrganizationId()))
      .thenReturn(CompletableFuture.completedFuture("workflowId"));

    workflow.retrieve();

    verify(brokersRetrieverActivityMock, times(1)).fetchAll();
    verify(organizationBrokeredRetrieverActivityMock, times(1)).retrieve(1L);
    verify(organizationPaymentsReportingPagoPaFetchWFClientMock, times(1)).retrieveAsyncStart(organization.getOrganizationId());
  }

  @Test
  void retrieveWithNoBrokers() {
    when(brokersRetrieverActivityMock.fetchAll()).thenReturn(Collections.emptyList());

    workflow.retrieve();

    verify(brokersRetrieverActivityMock, times(1)).fetchAll();
    verify(organizationBrokeredRetrieverActivityMock, never()).retrieve(anyLong());
    verify(organizationPaymentsReportingPagoPaFetchWFClientMock, never()).retrieve(anyLong());
  }

  @Test
  void retrieveWithNoOrganizations() {
    Broker broker = new Broker().brokerId(1L);

    when(brokersRetrieverActivityMock.fetchAll()).thenReturn(List.of(broker));
    when(organizationBrokeredRetrieverActivityMock.retrieve(1L)).thenReturn(Collections.emptyList());

    workflow.retrieve();

    verify(brokersRetrieverActivityMock, times(1)).fetchAll();
    verify(organizationBrokeredRetrieverActivityMock, times(1)).retrieve(1L);
    verify(organizationPaymentsReportingPagoPaFetchWFClientMock, never()).retrieve(anyLong());
  }

  @Test
  void retrieveWithExceptionInBrokersRetriever() {
    when(brokersRetrieverActivityMock.fetchAll()).thenThrow(new RuntimeException("Error"));

    assertThrows(RuntimeException.class, () -> workflow.retrieve());

    verify(brokersRetrieverActivityMock, times(1)).fetchAll();
    verify(organizationBrokeredRetrieverActivityMock, never()).retrieve(anyLong());
    verify(organizationPaymentsReportingPagoPaFetchWFClientMock, never()).retrieve(anyLong());
  }

  @Test
  void retrieveWithExceptionInOrganizationRetriever() {
    Broker broker = new Broker().brokerId(1L);

    when(brokersRetrieverActivityMock.fetchAll()).thenReturn(List.of(broker));
    when(organizationBrokeredRetrieverActivityMock.retrieve(1L)).thenThrow(new RuntimeException("Error"));

    assertThrows(RuntimeException.class, () -> workflow.retrieve());

    verify(brokersRetrieverActivityMock, times(1)).fetchAll();
    verify(organizationBrokeredRetrieverActivityMock, times(1)).retrieve(1L);
    verify(organizationPaymentsReportingPagoPaFetchWFClientMock, never()).retrieve(anyLong());
  }
}

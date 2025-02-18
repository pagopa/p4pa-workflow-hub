package it.gov.pagopa.pu.workflow.wf.paymentsreporting.broker.wfretrieve;

import it.gov.pagopa.payhub.activities.activity.organization.BrokersRetrieverActivity;
import it.gov.pagopa.payhub.activities.activity.organization.OrganizationBrokeredRetrieverActivity;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflow.wf.paymentsreporting.broker.activity.ChainOrganizationsBrokered2OrganizationPaymentsReportingActivity;
import it.gov.pagopa.pu.workflow.wf.paymentsreporting.broker.config.OrganizationsBrokeredRetrieveWFConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationsBrokeredRetrieveWFTest {

  @Mock
  private BrokersRetrieverActivity brokersRetrieverActivityMock;

  @Mock
  private OrganizationBrokeredRetrieverActivity organizationBrokeredRetrieverActivityMock;

  @Mock
  private ChainOrganizationsBrokered2OrganizationPaymentsReportingActivity chainOrganizationsBrokered2OrganizationPaymentsReportingActivityMock;

  private OrganizationsBrokeredRetrieveWFImpl workflow;

  @BeforeEach
  void setUp() {
    OrganizationsBrokeredRetrieveWFConfig configMock = mock(OrganizationsBrokeredRetrieveWFConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);
    when(configMock.buildBrokersRetrieverActivityStub()).thenReturn(brokersRetrieverActivityMock);
    when(configMock.buildOrganizationBrokeredRetrieverActivityStub()).thenReturn(organizationBrokeredRetrieverActivityMock);
    when(configMock.buildChainBrokeredOrganizations2OrganizationPaymentsReportingActivityStub()).thenReturn(chainOrganizationsBrokered2OrganizationPaymentsReportingActivityMock);

    when(applicationContextMock.getBean(OrganizationsBrokeredRetrieveWFConfig.class)).thenReturn(configMock);

    workflow = new OrganizationsBrokeredRetrieveWFImpl();
    workflow.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(brokersRetrieverActivityMock, organizationBrokeredRetrieverActivityMock, chainOrganizationsBrokered2OrganizationPaymentsReportingActivityMock);
  }

  @Test
  void retrieveWithValidData() {
    Broker broker = new Broker().brokerId(1L);
    Organization organization = new Organization().organizationId(1L);

    when(brokersRetrieverActivityMock.fetchAll()).thenReturn(List.of(broker));
    when(organizationBrokeredRetrieverActivityMock.retrieve(1L)).thenReturn(List.of(organization));

    when(brokersRetrieverActivityMock.fetchAll()).thenReturn(List.of(broker));
    when(organizationBrokeredRetrieverActivityMock.retrieve(1L)).thenReturn(List.of(organization));

    workflow.retrieve();

    verify(brokersRetrieverActivityMock, times(1)).fetchAll();
    verify(organizationBrokeredRetrieverActivityMock, times(1)).retrieve(1L);
    verify(chainOrganizationsBrokered2OrganizationPaymentsReportingActivityMock, times(1)).chain(1L);
  }

  @Test
  void retrieveWithNoBrokers() {
    when(brokersRetrieverActivityMock.fetchAll()).thenReturn(Collections.emptyList());

    workflow.retrieve();

    verify(brokersRetrieverActivityMock, times(1)).fetchAll();
    verify(organizationBrokeredRetrieverActivityMock, never()).retrieve(anyLong());
    verify(chainOrganizationsBrokered2OrganizationPaymentsReportingActivityMock, never()).chain(anyLong());
  }

  @Test
  void retrieveWithNoOrganizations() {
    Broker broker = new Broker().brokerId(1L);

    when(brokersRetrieverActivityMock.fetchAll()).thenReturn(List.of(broker));
    when(organizationBrokeredRetrieverActivityMock.retrieve(1L)).thenReturn(Collections.emptyList());

    workflow.retrieve();

    verify(brokersRetrieverActivityMock, times(1)).fetchAll();
    verify(organizationBrokeredRetrieverActivityMock, times(1)).retrieve(1L);
    verify(chainOrganizationsBrokered2OrganizationPaymentsReportingActivityMock, never()).chain(anyLong());
  }

  @Test
  void retrieveWithExceptionInBrokersRetriever() {
    when(brokersRetrieverActivityMock.fetchAll()).thenThrow(new RuntimeException("Error"));

    assertThrows(RuntimeException.class, () -> workflow.retrieve());

    verify(brokersRetrieverActivityMock, times(1)).fetchAll();
    verify(organizationBrokeredRetrieverActivityMock, never()).retrieve(anyLong());
    verify(chainOrganizationsBrokered2OrganizationPaymentsReportingActivityMock, never()).chain(anyLong());
  }

  @Test
  void retrieveWithExceptionInOrganizationRetriever() {
    Broker broker = new Broker().brokerId(1L);

    when(brokersRetrieverActivityMock.fetchAll()).thenReturn(List.of(broker));
    when(organizationBrokeredRetrieverActivityMock.retrieve(1L)).thenThrow(new RuntimeException("Error"));

    assertThrows(RuntimeException.class, () -> workflow.retrieve());

    verify(brokersRetrieverActivityMock, times(1)).fetchAll();
    verify(organizationBrokeredRetrieverActivityMock, times(1)).retrieve(1L);
    verify(chainOrganizationsBrokered2OrganizationPaymentsReportingActivityMock, never()).chain(anyLong());
  }
}

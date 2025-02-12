package it.gov.pagopa.pu.workflow.wf.pagopa.paymentreporting.wffetch;

import it.gov.pagopa.payhub.activities.activity.paymentsreporting.OrganizationPaymentsReportingPagoPaListRetrieverActivity;
import it.gov.pagopa.payhub.activities.activity.paymentsreporting.OrganizationPaymentsReportingPagoPaRetrieverActivity;
import it.gov.pagopa.payhub.activities.exception.NotRetryableActivityException;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentreporting.config.PaymentsReportingPagoPaWfConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationPaymentsReportingPagoPaFetchWFTest {

  @Mock
  private OrganizationPaymentsReportingPagoPaListRetrieverActivity organizationPaymentsReportingPagoPaListRetrieverActivityMock;
  @Mock
  private OrganizationPaymentsReportingPagoPaRetrieverActivity organizationPaymentsReportingPagoPaRetrieverActivityMock;

  private OrganizationPaymentsReportingPagoPaFetchWFImpl wf;

  @BeforeEach
  void setUp() {
    PaymentsReportingPagoPaWfConfig paymentsReportingPagoPaWfConfigMock = mock(PaymentsReportingPagoPaWfConfig.class);
    ApplicationContext applicationContextMock = mock(ApplicationContext.class);
    when(paymentsReportingPagoPaWfConfigMock.buildOrganizationPaymentsReportingPagoPaListRetrieverActivityStub()).thenReturn(organizationPaymentsReportingPagoPaListRetrieverActivityMock);
    when(paymentsReportingPagoPaWfConfigMock.buildOrganizationPaymentsReportingPagoPaRetrieverActivityStub()).thenReturn(organizationPaymentsReportingPagoPaRetrieverActivityMock);

    when(applicationContextMock.getBean(PaymentsReportingPagoPaWfConfig.class)).thenReturn(paymentsReportingPagoPaWfConfigMock);

    wf = new OrganizationPaymentsReportingPagoPaFetchWFImpl();
    wf.setApplicationContext(applicationContextMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      organizationPaymentsReportingPagoPaListRetrieverActivityMock,
      organizationPaymentsReportingPagoPaRetrieverActivityMock);
  }

  @Test
  void givenSuccessfulProcessingConditionWhenRetrieveThenOk() {
    // Given
    Long organizationId = 1L;
    List<PaymentsReportingIdDTO> paymentsReportingIds = List.of(new PaymentsReportingIdDTO());

    when(organizationPaymentsReportingPagoPaListRetrieverActivityMock.retrieve(organizationId))
      .thenReturn(paymentsReportingIds);
    when(organizationPaymentsReportingPagoPaRetrieverActivityMock.fetch(organizationId, paymentsReportingIds))
      .thenReturn(List.of(1L));

    // When
    wf.retrieve(organizationId);

    // Then
    verify(organizationPaymentsReportingPagoPaListRetrieverActivityMock).retrieve(organizationId);
    verify(organizationPaymentsReportingPagoPaRetrieverActivityMock).fetch(organizationId, paymentsReportingIds);
  }

  @Test
  void givenFailingProcessingConditionWhenIngestThenKo() {
    // Given
    Long organizationId = 1L;
    doThrow(new NotRetryableActivityException("Test exception"))
      .when(organizationPaymentsReportingPagoPaListRetrieverActivityMock).retrieve(organizationId);

    // When
    assertThrows(NotRetryableActivityException.class, () -> wf.retrieve(organizationId));
    verify(organizationPaymentsReportingPagoPaListRetrieverActivityMock).retrieve(organizationId);
  }
}

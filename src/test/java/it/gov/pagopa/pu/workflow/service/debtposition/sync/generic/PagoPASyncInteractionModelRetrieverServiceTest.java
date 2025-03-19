package it.gov.pagopa.pu.workflow.service.debtposition.sync.generic;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.workflow.connector.organization.service.BrokerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PagoPASyncInteractionModelRetrieverServiceTest {

  @Mock
  private BrokerService brokerServiceMock;

  private PagoPASyncInteractionModelRetrieverService service;

  @BeforeEach
  void init(){
    service = new PagoPASyncInteractionModelRetrieverService(brokerServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(brokerServiceMock);
  }

  @Test
  void givenNotExistentBrokerWhenRetrieveInteractionModelThenIllegalStateException(){
    // Given
    long organizationId = 1L;
    String accessToken = "ACCESSTOKEN";

    Mockito.when(brokerServiceMock.findByBrokeredOrganizationId(organizationId, accessToken))
      .thenReturn(Optional.empty());

    // When, Then
    Assertions.assertThrows(IllegalStateException.class, () -> service.retrieveInteractionModel(organizationId, accessToken));
  }

  @Test
  void whenRetrieveInteractionModelThenInvokeService(){
    // Given
    long organizationId = 1L;
    String accessToken = "ACCESSTOKEN";

    Broker broker = new Broker();
    broker.setPagoPaInteractionModel(Broker.PagoPaInteractionModelEnum.SYNC);

    Mockito.when(brokerServiceMock.findByBrokeredOrganizationId(organizationId, accessToken))
      .thenReturn(Optional.of(broker));

    // When
    Broker.PagoPaInteractionModelEnum result = service.retrieveInteractionModel(organizationId, accessToken);

    // Then
    Assertions.assertSame(broker.getPagoPaInteractionModel(), result);
  }
}

package it.gov.pagopa.pu.workflow.service.wf.debtposition.sync.complete.generic;

import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;
import it.gov.pagopa.pu.organization.dto.generated.PagoPaInteractionModel;
import it.gov.pagopa.pu.workflow.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.workflow.exception.custom.IllegalStateBusinessException;
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
  private OrganizationService organizationServiceMock;

  private PagoPASyncInteractionModelRetrieverService service;

  @BeforeEach
  void init(){
    service = new PagoPASyncInteractionModelRetrieverService(organizationServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(organizationServiceMock);
  }

  @Test
  void givenNotExistentStationWhenRetrieveInteractionModelThenIllegalStateException(){
    // Given
    long organizationId = 1L;
    String accessToken = "ACCESSTOKEN";
    String stationId = "STATIONNULL";

    Mockito.when(organizationServiceMock.findOrganizationStation(organizationId, stationId, accessToken))
      .thenReturn(Optional.empty());

    // When, Then
    Assertions.assertThrows(IllegalStateBusinessException.class, () -> service.retrieveInteractionModel(organizationId, stationId, accessToken));
  }

  @Test
  void whenRetrieveInteractionModelThenInvokeService(){
    // Given
    long organizationId = 1L;
    String accessToken = "ACCESSTOKEN";
    String stationId = "STATIONID";

    OrganizationStationDTO organizationStationDTO = new OrganizationStationDTO();
    organizationStationDTO.setPagoPaInteractionModel(PagoPaInteractionModel.SYNC);

    Mockito.when(organizationServiceMock.findOrganizationStation(organizationId, stationId, accessToken))
      .thenReturn(Optional.of(organizationStationDTO));

    // When
    PagoPaInteractionModel  result = service.retrieveInteractionModel(organizationId, stationId, accessToken);

    // Then
    Assertions.assertSame(organizationStationDTO.getPagoPaInteractionModel(), result);
  }
}

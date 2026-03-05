package it.gov.pagopa.pu.workflow.connector.organization.service;

import it.gov.pagopa.payhub.activities.connector.auth.AuthnService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflow.connector.organization.client.OrganizationSearchClient;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationRetrieverServiceTest {

    @Mock
    private OrganizationSearchClient organizationSearchClientMock;
    @Mock
    private AuthnService authnServiceMock;

    private OrganizationService organizationService;

    private final String accessToken = "ACCESSTOKEN";

    @BeforeEach
    void init(){
        organizationService = new OrganizationServiceImpl(
          organizationSearchClientMock,
          authnServiceMock
        );
    }

    @AfterEach
    void verifyNoMoreInteractions(){
        Mockito.verifyNoMoreInteractions(
            organizationSearchClientMock
        );
    }

//region getOrganizationByFiscalCode tests
    @Test
    void givenNotExistentFiscalCodeWhenGetOrganizationByFiscalCodeThenEmpty(){
        // Given
        String orgFiscalCode = "ORGFISCALCODE";
        Mockito.when(organizationSearchClientMock.findByOrgFiscalCode(orgFiscalCode, accessToken))
                .thenReturn(null);
        Mockito.when(authnServiceMock.getAccessToken()).thenReturn(accessToken);


        // When
        Optional<Organization> result = organizationService.getOrganizationByFiscalCode(orgFiscalCode);

        // Then
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void givenExistentFiscalCodeWhenGetOrganizationByFiscalCodeThenEmpty(){
        // Given
        String orgFiscalCode = "ORGFISCALCODE";
        Organization expectedResult = new Organization();
        Mockito.when(organizationSearchClientMock.findByOrgFiscalCode(orgFiscalCode, accessToken))
                .thenReturn(expectedResult);
        Mockito.when(authnServiceMock.getAccessToken()).thenReturn(accessToken);

        // When
        Optional<Organization> result = organizationService.getOrganizationByFiscalCode(orgFiscalCode);

        // Then
        Assertions.assertTrue(result.isPresent());
        Assertions.assertSame(expectedResult, result.get());
    }
//endregion

  @Test
  void whenGetOrganizationByIdThenInvokeClient() {
    Long organizationId = 1L;
    Organization expectedResult = new Organization();

    when(organizationSearchClientMock.findById(organizationId, accessToken))
      .thenReturn(expectedResult);

    Organization result = organizationService.getOrganizationById(organizationId, accessToken);

    assertSame(expectedResult, result);
  }
}

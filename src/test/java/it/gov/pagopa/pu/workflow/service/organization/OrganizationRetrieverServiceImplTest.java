package it.gov.pagopa.pu.workflow.service.organization;

import it.gov.pagopa.payhub.activities.connector.auth.AuthnService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflow.connector.organization.service.OrganizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationRetrieverServiceImplTest {

  @Mock
  private OrganizationService organizationService;

  @Mock
  private AuthnService authnService;

  @InjectMocks
  private OrganizationRetrieverServiceImpl organizationRetrieverService;

  @Test
  void returnTrueWhenClassificationIsEnabled() {
    Long orgId = 123L;
    String accessToken = "accessToken";

    Organization org = new Organization();
    org.setFlagClassification(true);

    when(authnService.getAccessToken()).thenReturn(accessToken);
    when(organizationService.getOrganizationById(orgId, accessToken)).thenReturn(org);

    boolean result = organizationRetrieverService.isClassificationEnabled(orgId);

    assertTrue(result);
  }

  @Test
  void returnFalseWhenClassificationIsDisabled() {
    Long orgId = 123L;
    String accessToken = "accessToken";

    Organization org = new Organization();
    org.setFlagClassification(false);

    when(authnService.getAccessToken()).thenReturn(accessToken);
    when(organizationService.getOrganizationById(orgId, accessToken)).thenReturn(org);

    boolean result = organizationRetrieverService.isClassificationEnabled(orgId);

    assertFalse(result);
  }
}

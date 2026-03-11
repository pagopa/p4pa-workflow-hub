package it.gov.pagopa.pu.workflow.service.organization;

import it.gov.pagopa.payhub.activities.connector.auth.AuthnService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflow.connector.organization.service.OrganizationService;
import org.springframework.stereotype.Service;

@Service
public class OrganizationRetrieverServiceImpl implements OrganizationRetrieverService {

  private final OrganizationService organizationService;
  private final AuthnService authnService;

  public OrganizationRetrieverServiceImpl(OrganizationService organizationService, AuthnService authnService) {
    this.organizationService = organizationService;
    this.authnService = authnService;
  }

  @Override
  public boolean isClassificationEnabled(Long organizationId) {
    Organization org = organizationService.getOrganizationById(organizationId, authnService.getAccessToken());
    return org.getFlagClassification();
  }
}

package it.gov.pagopa.pu.workflow.connector.organization.service;

import it.gov.pagopa.payhub.activities.connector.auth.AuthnService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflow.connector.organization.client.OrganizationSearchClient;
import java.util.Optional;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@CacheConfig(cacheNames = it.gov.pagopa.pu.workflow.config.CacheConfig.Fields.organization)
public class OrganizationServiceImpl implements OrganizationService {

  private final OrganizationSearchClient organizationSearchClient;
  private final AuthnService authnService;

  public OrganizationServiceImpl(OrganizationSearchClient organizationSearchClient,
    AuthnService authnService) {
    this.organizationSearchClient = organizationSearchClient;
    this.authnService = authnService;
  }

  @Override
  @Cacheable(key = "'fiscalCode-' + #orgFiscalCode", unless = "#result == null")
  public Optional<Organization> getOrganizationByFiscalCode(String orgFiscalCode) {
    return Optional.ofNullable(
      organizationSearchClient.findByOrgFiscalCode(orgFiscalCode, authnService.getAccessToken())
    );
  }
}

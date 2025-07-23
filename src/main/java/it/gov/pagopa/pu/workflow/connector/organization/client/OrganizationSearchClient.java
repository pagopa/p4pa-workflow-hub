package it.gov.pagopa.pu.workflow.connector.organization.client;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflow.connector.organization.config.OrganizationApisHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
public class OrganizationSearchClient {

  private final OrganizationApisHolder organizationApisHolder;

  public OrganizationSearchClient(OrganizationApisHolder organizationApisHolder) {
    this.organizationApisHolder = organizationApisHolder;
  }

  public Organization findByOrgFiscalCode(String orgFiscalCode, String accessToken) {
    try{
      return organizationApisHolder.getOrganizationSearchControllerApi(accessToken)
        .crudOrganizationsFindByOrgFiscalCode(orgFiscalCode);
    } catch (HttpClientErrorException.NotFound e){
      log.info("Cannot find organization having fiscalCode {}", orgFiscalCode);
      return null;
    }
  }

}

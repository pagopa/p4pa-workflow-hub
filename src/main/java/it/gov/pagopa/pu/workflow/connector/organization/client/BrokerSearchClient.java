package it.gov.pagopa.pu.workflow.connector.organization.client;

import it.gov.pagopa.pu.workflow.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import org.springframework.stereotype.Service;

@Service
public class BrokerSearchClient {

  private final OrganizationApisHolder organizationApisHolder;

  public BrokerSearchClient(OrganizationApisHolder organizationApisHolder) {
    this.organizationApisHolder = organizationApisHolder;
  }

  public Broker findByBrokeredOrganizationId(Long orgId, String accessToken) {
    return organizationApisHolder.getBrokerSearchControllerApi(accessToken)
      .crudBrokersFindByBrokeredOrganizationId(String.valueOf(orgId));
  }

}

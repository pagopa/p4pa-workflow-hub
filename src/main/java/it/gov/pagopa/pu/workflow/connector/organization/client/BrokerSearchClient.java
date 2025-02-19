package it.gov.pagopa.pu.workflow.connector.organization.client;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.workflow.connector.organization.config.OrganizationApisHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
public class BrokerSearchClient {

  private final OrganizationApisHolder organizationApisHolder;

  public BrokerSearchClient(OrganizationApisHolder organizationApisHolder) {
    this.organizationApisHolder = organizationApisHolder;
  }

  public Broker findByBrokeredOrganizationId(Long orgId, String accessToken) {
    try{
      return organizationApisHolder.getBrokerSearchControllerApi(accessToken)
        .crudBrokersFindByBrokeredOrganizationId(String.valueOf(orgId));
    } catch (HttpClientErrorException.NotFound e){
      log.info("Cannot find Broker related do organization having id {}", orgId);
      return null;
    }
  }

}

package it.gov.pagopa.pu.workflow.connector.organization.service;

import it.gov.pagopa.pu.workflow.connector.organization.client.BrokerSearchClient;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@CacheConfig(cacheNames = it.gov.pagopa.pu.workflow.config.CacheConfig.Fields.organization)
public class BrokerServiceImpl implements BrokerService {

  private final BrokerSearchClient brokerSearchClient;

  public BrokerServiceImpl(BrokerSearchClient brokerSearchClient) {
    this.brokerSearchClient = brokerSearchClient;
  }

  @Override
  @Cacheable(key = "'brokerOf-' + #organizationId", unless = "#result == null")
  public Optional<Broker> findByBrokeredOrganizationId(Long organizationId, String accessToken) {
    return Optional.ofNullable(
      brokerSearchClient.findByBrokeredOrganizationId(organizationId, accessToken)
    );
  }
}

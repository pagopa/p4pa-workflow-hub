package it.gov.pagopa.pu.workflow.connector.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.Broker;

import java.util.Optional;

public interface BrokerService {

  Optional<Broker> findByBrokeredOrganizationId(Long organizationId, String accessToken);
}

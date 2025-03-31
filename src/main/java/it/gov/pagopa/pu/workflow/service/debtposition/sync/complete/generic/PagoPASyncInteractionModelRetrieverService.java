package it.gov.pagopa.pu.workflow.service.debtposition.sync.complete.generic;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.PagoPaInteractionModel;
import it.gov.pagopa.pu.workflow.connector.organization.service.BrokerService;
import org.springframework.stereotype.Service;

@Service
public class PagoPASyncInteractionModelRetrieverService {

  private final BrokerService brokerService;

  public PagoPASyncInteractionModelRetrieverService(BrokerService brokerService) {
    this.brokerService = brokerService;
  }

  public PagoPaInteractionModel retrieveInteractionModel(long organizationId, String accessToken){
    return brokerService.findByBrokeredOrganizationId(organizationId, accessToken)
      .map(Broker::getPagoPaInteractionModel)
      .orElseThrow(() -> new IllegalStateException("Cannot find a broker for organization " + organizationId));
  }
}

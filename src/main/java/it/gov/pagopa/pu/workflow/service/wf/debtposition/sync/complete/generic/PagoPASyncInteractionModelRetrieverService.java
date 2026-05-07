package it.gov.pagopa.pu.workflow.service.wf.debtposition.sync.complete.generic;

import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;
import it.gov.pagopa.pu.organization.dto.generated.PagoPaInteractionModel;
import it.gov.pagopa.pu.workflow.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.workflow.exception.custom.IllegalStateBusinessException;
import it.gov.pagopa.pu.workflow.utilities.ErrorCodeConstants;
import org.springframework.stereotype.Service;

@Service
public class PagoPASyncInteractionModelRetrieverService {

  private final OrganizationService organizationService;

  public PagoPASyncInteractionModelRetrieverService(OrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  public PagoPaInteractionModel retrieveInteractionModel(long organizationId, String stationId, String accessToken){
    return organizationService.findOrganizationStation(organizationId, stationId, accessToken)
      .map(OrganizationStationDTO::getPagoPaInteractionModel)
      .orElseThrow(() -> new IllegalStateBusinessException(ErrorCodeConstants.ERROR_CODE_STATION_NOT_FOUND,
        String.format("Cannot find a Station for organizationId %s and stationId %s",  organizationId, stationId)));
  }
}

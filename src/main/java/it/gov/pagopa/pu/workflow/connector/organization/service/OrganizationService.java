package it.gov.pagopa.pu.workflow.connector.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;

import java.util.Optional;

public interface OrganizationService {

  Optional<Organization> getOrganizationByFiscalCode(String orgFiscalCode);

  Organization getOrganizationById(Long organizationId, String accessToken);

  Optional<OrganizationStationDTO> findOrganizationStation(Long organizationId, String stationId, String accessToken);
}

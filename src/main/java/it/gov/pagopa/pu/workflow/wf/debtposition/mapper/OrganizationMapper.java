package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflow.dto.generated.OrganizationRequestDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {LinkMapper.class})
public interface OrganizationMapper {

  Organization map(OrganizationRequestDTO organizationRequestDTO);
}

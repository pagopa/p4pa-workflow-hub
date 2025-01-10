package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionTypeOrgDTO;
import it.gov.pagopa.pu.workflow.dto.generated.DebtPositionTypeOrgRequestDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {OrganizationMapper.class, DebtPositionTypeMapper.class})
public interface DebtPositionTypeOrgMapper {

  DebtPositionTypeOrgDTO map(DebtPositionTypeOrgRequestDTO debtPositionTypeOrgRequestDTO);
}

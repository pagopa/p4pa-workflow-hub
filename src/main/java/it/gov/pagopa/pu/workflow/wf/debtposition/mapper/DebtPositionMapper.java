package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.DebtPositionRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {IngestionFlowFileMapper.class, OrganizationMapper.class, DebtPositionTypeOrgMapper.class, PaymentOptionMapper.class})
public interface DebtPositionMapper {

  @Mapping(target = "gpdStatus", expression = "java(debtPositionRequestDTO.getGpdStatus() != null && !debtPositionRequestDTO.getGpdStatus().isEmpty() ? debtPositionRequestDTO.getGpdStatus().charAt(0) : '\\0')")
  DebtPositionDTO map(DebtPositionRequestDTO debtPositionRequestDTO);
}

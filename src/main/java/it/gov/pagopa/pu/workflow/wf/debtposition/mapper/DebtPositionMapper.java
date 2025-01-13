package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.DebtPositionRequestDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PaymentOptionMapper.class})
public interface DebtPositionMapper {

  DebtPositionDTO map(DebtPositionRequestDTO debtPositionRequestDTO);
}

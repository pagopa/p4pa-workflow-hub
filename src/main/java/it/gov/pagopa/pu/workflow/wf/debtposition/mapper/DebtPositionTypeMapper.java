package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionTypeDTO;
import it.gov.pagopa.pu.workflow.dto.generated.DebtPositionTypeRequestDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DebtPositionTypeMapper {

  DebtPositionTypeDTO map(DebtPositionTypeRequestDTO debtPositionTypeRequest);
}

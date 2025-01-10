package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.payhub.activities.dto.PersonDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PersonRequestDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PersonMapper {

  PersonDTO map(PersonRequestDTO personRequestDTO);
}

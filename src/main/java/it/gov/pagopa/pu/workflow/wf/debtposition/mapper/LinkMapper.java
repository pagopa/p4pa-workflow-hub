package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.pu.organization.dto.generated.Link;
import it.gov.pagopa.pu.workflow.dto.generated.LinkRequestDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LinkMapper {

  Link map(LinkRequestDTO linkRequestDTO);
}

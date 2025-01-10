package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.payhub.activities.dto.IngestionFlowFileDTO;
import it.gov.pagopa.pu.workflow.dto.generated.IngestionFlowFileRequestDTO;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {IngestionFlowFileTypeMapper.class, OrganizationMapper.class, Utilities.class})
public interface IngestionFlowFileMapper {

  @Mapping(source = "creationDate", target = "creationDate", qualifiedByName = "offsetDateTimeToInstant")
  @Mapping(source = "lastUpdateDate", target = "lastUpdateDate", qualifiedByName = "offsetDateTimeToInstant")
  @Mapping(source = "flowDateTime", target = "flowDateTime", qualifiedByName = "offsetDateTimeToLocalDateTime")
  IngestionFlowFileDTO map(IngestionFlowFileRequestDTO ingestionFlowFileRequestDTO);
}

package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.payhub.activities.dto.TransferDTO;
import it.gov.pagopa.pu.workflow.dto.generated.TransferRequestDTO;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {Utilities.class})
public interface TransferMapper {

  @Mapping(source = "creationDate", target = "creationDate", qualifiedByName = "offsetDateTimeToInstant")
  @Mapping(source = "lastUpdateDate", target = "lastUpdateDate", qualifiedByName = "offsetDateTimeToInstant")
  TransferDTO map(TransferRequestDTO transferRequestDTO);


}


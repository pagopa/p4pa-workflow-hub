package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.payhub.activities.dto.ReceiptDTO;
import it.gov.pagopa.pu.workflow.dto.generated.ReceiptRequestDTO;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PersonMapper.class, Utilities.class})
public interface ReceiptMapper {

  @Mapping(source = "creationDate", target = "creationDate", qualifiedByName = "offsetDateTimeToInstant")
  @Mapping(source = "dtProcessing", target = "dtProcessing", qualifiedByName = "offsetDateTimeToInstant")
  ReceiptDTO map(ReceiptRequestDTO receiptRequestDTO);
}

package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.payhub.activities.dto.debtposition.InstallmentDTO;
import it.gov.pagopa.pu.workflow.dto.generated.InstallmentRequestDTO;
import it.gov.pagopa.pu.workflow.utilities.Utilities;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {TransferMapper.class, ReceiptMapper.class, PersonMapper.class, Utilities.class})
public interface InstallmentMapper {

  @Mapping(source = "creationDate", target = "creationDate", qualifiedByName = "offsetDateTimeToInstant")
  @Mapping(source = "updateDate", target = "updateDate", qualifiedByName = "offsetDateTimeToInstant")
  @Mapping(source = "iuvCreationDate", target = "iuvCreationDate", qualifiedByName = "offsetDateTimeToInstant")
  InstallmentDTO map(InstallmentRequestDTO installmentRequestDTO);
}

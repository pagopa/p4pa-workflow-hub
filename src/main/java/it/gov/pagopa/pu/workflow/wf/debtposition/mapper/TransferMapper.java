package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.pu.debtposition.dto.generated.TransferDTO;
import it.gov.pagopa.pu.workflow.dto.generated.TransferRequestDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransferMapper {

  TransferDTO map(TransferRequestDTO transferRequestDTO);


}


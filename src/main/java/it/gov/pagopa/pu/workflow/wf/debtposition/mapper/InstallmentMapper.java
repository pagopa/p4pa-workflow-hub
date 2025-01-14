package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.workflow.dto.generated.InstallmentRequestDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {TransferMapper.class, PersonMapper.class})
public interface InstallmentMapper {

  InstallmentDTO map(InstallmentRequestDTO installmentRequestDTO);
}

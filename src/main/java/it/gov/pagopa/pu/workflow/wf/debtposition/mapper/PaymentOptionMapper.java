package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.pu.debtposition.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentOptionRequestDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {InstallmentMapper.class})
public interface PaymentOptionMapper {

  PaymentOptionDTO map(PaymentOptionRequestDTO paymentOptionRequestDTO);
}

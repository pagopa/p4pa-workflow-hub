package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.payhub.activities.dto.debtposition.PaymentOptionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentOptionRequestDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PaymentOptionTypeMapper.class, InstallmentMapper.class, OrganizationMapper.class})
public interface PaymentOptionMapper {

  PaymentOptionDTO map(PaymentOptionRequestDTO paymentOptionRequestDTO);
}

package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.payhub.activities.enums.PaymentOptionType;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentOptionTypeRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentOptionTypeMapper {

  PaymentOptionType map(PaymentOptionTypeRequest paymentOptionTypeRequest);
}


package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.pu.debtposition.dto.generated.PaymentOptionDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.workflow.utils.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.InstallmentFaker.buildInstallmentRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.PaymentOptionFaker.buildPaymentOptionDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.PaymentOptionFaker.buildPaymentOptionRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PaymentOptionMapperTest {

  @Mock
  private InstallmentMapper installmentMapperMock;

  @InjectMocks
  private final PaymentOptionMapper mapper = Mappers.getMapper(PaymentOptionMapper.class);

  @Test
  void testMapPaymentOptionDTO() {

    Mockito.when(installmentMapperMock.map(buildInstallmentRequestDTO()))
      .thenReturn(buildInstallmentDTO());

    PaymentOptionDTO paymentOption = mapper.map(buildPaymentOptionRequestDTO());

    PaymentOptionDTO expected = buildPaymentOptionDTO();

    checkNotNullFields(paymentOption);
    assertEquals(expected, paymentOption);

  }
}

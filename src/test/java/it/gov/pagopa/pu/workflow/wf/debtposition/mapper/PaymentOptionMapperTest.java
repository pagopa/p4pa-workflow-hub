package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.payhub.activities.dto.debtposition.PaymentOptionDTO;
import it.gov.pagopa.payhub.activities.enums.PaymentOptionType;
import it.gov.pagopa.pu.workflow.dto.generated.PaymentOptionTypeRequest;
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
import static it.gov.pagopa.pu.workflow.utils.faker.OrganizationFaker.buildOrganizationDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.OrganizationFaker.buildOrganizationRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.PaymentOptionFaker.buildPaymentOptionDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.PaymentOptionFaker.buildPaymentOptionRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PaymentOptionMapperTest {

  @Mock
  private OrganizationMapper organizationMapperMock;
  @Mock
  private InstallmentMapper installmentMapperMock;
  @Mock
  private PaymentOptionTypeMapper paymentOptionTypeMapperMock;

  @InjectMocks
  private final PaymentOptionMapper mapper = Mappers.getMapper(PaymentOptionMapper.class);

  @Test
  void testMapPaymentOptionDTO() {

    Mockito.when(organizationMapperMock.map(buildOrganizationRequestDTO()))
      .thenReturn(buildOrganizationDTO());

    Mockito.when(paymentOptionTypeMapperMock.map(PaymentOptionTypeRequest.DOWN_PAYMENT)).thenReturn(PaymentOptionType.DOWN_PAYMENT);

    Mockito.when(installmentMapperMock.map(buildInstallmentRequestDTO()))
      .thenReturn(buildInstallmentDTO());

    PaymentOptionDTO paymentOption = mapper.map(buildPaymentOptionRequestDTO());

    PaymentOptionDTO expected = buildPaymentOptionDTO();

    checkNotNullFields(paymentOption);
    assertEquals(expected, paymentOption);

  }
}

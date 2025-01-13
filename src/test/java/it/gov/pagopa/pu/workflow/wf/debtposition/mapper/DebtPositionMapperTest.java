package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionFaker.buildDebtPositionRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.PaymentOptionFaker.buildPaymentOptionDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.PaymentOptionFaker.buildPaymentOptionRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DebtPositionMapperTest {
  @Mock
  private PaymentOptionMapper paymentOptionMapperMock;

  @InjectMocks
  private final DebtPositionMapper mapper = Mappers.getMapper(DebtPositionMapper.class);

  @Test
  void testMapDebtPosition() {
    DebtPositionDTO expected = buildDebtPositionDTO();

    Mockito.when(paymentOptionMapperMock.map(buildPaymentOptionRequestDTO())).thenReturn(buildPaymentOptionDTO());

    DebtPositionDTO result = mapper.map(buildDebtPositionRequestDTO());

    assertEquals(expected, result);
    checkNotNullFields(result);
  }
}

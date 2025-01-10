package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionDTO;
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
import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrgDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrgRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.IngestionFlowFileFaker.buildIngestionFlowFileDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.IngestionFlowFileFaker.buildIngestionFlowFileRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.OrganizationFaker.buildOrganization;
import static it.gov.pagopa.pu.workflow.utils.faker.OrganizationFaker.buildOrganizationRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.PaymentOptionFaker.buildPaymentOptionDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.PaymentOptionFaker.buildPaymentOptionRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DebtPositionMapperTest {

  @Mock
  private OrganizationMapper organizationMapperMock;
  @Mock
  private DebtPositionTypeOrgMapper debtPositionTypeOrgMapperMock;
  @Mock
  private IngestionFlowFileMapper ingestionFlowFileMapperMock;
  @Mock
  private PaymentOptionMapper paymentOptionMapperMock;

  @InjectMocks
  private final DebtPositionMapper mapper = Mappers.getMapper(DebtPositionMapper.class);

  @Test
  void testMapDebtPosition() {
    DebtPositionDTO expected = buildDebtPositionDTO();

    Mockito.when(organizationMapperMock.map(buildOrganizationRequestDTO())).thenReturn(buildOrganization());
    Mockito.when(debtPositionTypeOrgMapperMock.map(buildDebtPositionTypeOrgRequestDTO())).thenReturn(buildDebtPositionTypeOrgDTO());
    Mockito.when(ingestionFlowFileMapperMock.map(buildIngestionFlowFileRequestDTO())).thenReturn(buildIngestionFlowFileDTO());
    Mockito.when(paymentOptionMapperMock.map(buildPaymentOptionRequestDTO())).thenReturn(buildPaymentOptionDTO());

    DebtPositionDTO result = mapper.map(buildDebtPositionRequestDTO());

    assertEquals(expected, result);
    checkNotNullFields(result);
  }
}

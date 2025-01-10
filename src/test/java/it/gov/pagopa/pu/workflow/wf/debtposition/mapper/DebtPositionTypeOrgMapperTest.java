package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionTypeOrgDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionTypeFaker.buildDebtPositionType;
import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionTypeFaker.buildDebtPositionTypeRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrgDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrgRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.OrganizationFaker.buildOrganization;
import static it.gov.pagopa.pu.workflow.utils.faker.OrganizationFaker.buildOrganizationRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgMapperTest {

  @Mock
  private OrganizationMapper organizationMapperMock;
  @Mock
  private DebtPositionTypeMapper debtPositionTypeMapperMock;

  @InjectMocks
  private final DebtPositionTypeOrgMapper mapper = Mappers.getMapper(DebtPositionTypeOrgMapper.class);


  @Test
  void testMapPositionTypeOrg() {
    DebtPositionTypeOrgDTO expected = buildDebtPositionTypeOrgDTO();

    Mockito.when((organizationMapperMock.map(buildOrganizationRequestDTO())))
      .thenReturn(buildOrganization());
    Mockito.when(debtPositionTypeMapperMock.map(buildDebtPositionTypeRequestDTO()))
      .thenReturn(buildDebtPositionType());

    DebtPositionTypeOrgDTO result =
      mapper.map(buildDebtPositionTypeOrgRequestDTO());

    assertNotNull(result);
    checkNotNullFields(result);
    assertEquals(expected, result);
  }
}

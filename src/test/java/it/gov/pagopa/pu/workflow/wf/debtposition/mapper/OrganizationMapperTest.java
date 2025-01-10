package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.payhub.activities.dto.OrganizationDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.workflow.utils.faker.OrganizationFaker.buildOrganizationDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.OrganizationFaker.buildOrganizationRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OrganizationMapperTest {

  @InjectMocks
  private final OrganizationMapper mapper = Mappers.getMapper(OrganizationMapper.class);

  @Test
  void testMapOrganization() {
    OrganizationDTO expected = buildOrganizationDTO();

    OrganizationDTO organizationDTO =
      mapper.map(buildOrganizationRequestDTO());

    checkNotNullFields(organizationDTO);
    assertEquals(expected, organizationDTO);
  }
}

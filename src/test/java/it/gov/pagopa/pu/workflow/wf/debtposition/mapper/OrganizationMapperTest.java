package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.workflow.utils.faker.LinkFaker.buildLink;
import static it.gov.pagopa.pu.workflow.utils.faker.LinkFaker.buildLinkRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.OrganizationFaker.buildOrganization;
import static it.gov.pagopa.pu.workflow.utils.faker.OrganizationFaker.buildOrganizationRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OrganizationMapperTest {

  @Mock
  private LinkMapper linkMapperMock;

  @InjectMocks
  private final OrganizationMapper mapper = Mappers.getMapper(OrganizationMapper.class);

  @Test
  void testMapOrganization() {
    Organization expected = buildOrganization();

    Mockito.when(linkMapperMock.map(buildLinkRequestDTO())).thenReturn(buildLink());

    Organization organizationDTO =
      mapper.map(buildOrganizationRequestDTO());

    checkNotNullFields(organizationDTO);
    assertEquals(expected, organizationDTO);
  }
}

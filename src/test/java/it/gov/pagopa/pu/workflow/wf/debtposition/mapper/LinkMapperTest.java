package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.pu.organization.dto.generated.Link;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.workflow.utils.faker.LinkFaker.buildLink;
import static it.gov.pagopa.pu.workflow.utils.faker.LinkFaker.buildLinkRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class LinkMapperTest {

  @InjectMocks
  private final LinkMapper mapper = Mappers.getMapper(LinkMapper.class);

  @Test
  void testMapOrganization() {
    Link expected = buildLink();

    Link link =
      mapper.map(buildLinkRequestDTO());

    checkNotNullFields(link);
    assertEquals(expected, link);
  }
}

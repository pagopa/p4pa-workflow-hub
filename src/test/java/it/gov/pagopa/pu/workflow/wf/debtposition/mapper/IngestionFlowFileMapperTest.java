package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.payhub.activities.dto.IngestionFlowFileDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.workflow.utils.faker.IngestionFlowFileFaker.buildIngestionFlowFileDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.IngestionFlowFileFaker.buildIngestionFlowFileRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.IngestionFlowFileTypeFaker.buildIngestionFlowFileType;
import static it.gov.pagopa.pu.workflow.utils.faker.IngestionFlowFileTypeFaker.buildIngestionFlowFileTypeRequest;
import static it.gov.pagopa.pu.workflow.utils.faker.OrganizationFaker.buildOrganizationDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.OrganizationFaker.buildOrganizationRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class IngestionFlowFileMapperTest {

  @Mock
  private OrganizationMapper organizationMapperMock;
  @Mock
  private IngestionFlowFileTypeMapper ingestionFlowFileTypeMapperMock;

  @InjectMocks
  private final IngestionFlowFileMapper mapper = Mappers.getMapper(IngestionFlowFileMapper.class);

  @Test
  void testMapIngestionFlowDTO() {
    IngestionFlowFileDTO expected = buildIngestionFlowFileDTO();

    Mockito.when(ingestionFlowFileTypeMapperMock.map(buildIngestionFlowFileTypeRequest())).thenReturn(buildIngestionFlowFileType());
    Mockito.when(organizationMapperMock.map(buildOrganizationRequestDTO())).thenReturn(buildOrganizationDTO());

    IngestionFlowFileDTO result = mapper.map(buildIngestionFlowFileRequestDTO());

    assertEquals(expected, result);
    checkNotNullFields(result);
  }
}

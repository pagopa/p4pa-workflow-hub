package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.payhub.activities.dto.TransferDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.workflow.utils.faker.TransferFaker.buildTransferDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.TransferFaker.buildTransferRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class TransferMapperTest {

  @InjectMocks
  private final TransferMapper mapper = Mappers.getMapper(TransferMapper.class);

  @Test
  void givenMapThenSuccess() {
    TransferDTO expected = buildTransferDTO();

    TransferDTO result = mapper.map(buildTransferRequestDTO());

    checkNotNullFields(result);
    assertEquals(expected, result);
  }
}

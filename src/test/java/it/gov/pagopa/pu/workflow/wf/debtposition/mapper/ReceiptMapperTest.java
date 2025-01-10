package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.payhub.activities.dto.ReceiptDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.workflow.utils.faker.PersonFaker.buildPersonDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.PersonFaker.buildPersonRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.ReceiptFaker.buildReceiptDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.ReceiptFaker.buildReceiptRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ReceiptMapperTest {

  @Mock
  private PersonMapper personMapperMock;

  @InjectMocks
  private final ReceiptMapper mapper = Mappers.getMapper(ReceiptMapper.class);

  @Test
  void givenMapThenSuccess() {

    ReceiptDTO expected = buildReceiptDTO();

    Mockito.when(personMapperMock.map(buildPersonRequestDTO())).thenReturn(buildPersonDTO());

    Mockito.when(personMapperMock.map(buildPersonRequestDTO())).thenReturn(buildPersonDTO());

    ReceiptDTO result = mapper.map(buildReceiptRequestDTO());

    assertNotNull(result);
    checkNotNullFields(result);
    assertEquals(expected, result);

  }
}

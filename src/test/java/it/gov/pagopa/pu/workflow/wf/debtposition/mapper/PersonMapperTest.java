package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.pu.debtposition.dto.generated.PersonDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.workflow.utils.faker.PersonFaker.buildPersonDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.PersonFaker.buildPersonRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PersonMapperTest {

  @InjectMocks
  private final PersonMapper mapper = Mappers.getMapper(PersonMapper.class);

  @Test
  void testMapPersonDTO() {
    PersonDTO expected = buildPersonDTO();

    PersonDTO person = mapper.map(buildPersonRequestDTO());

    checkNotNullFields(person);
    assertEquals(expected, person);
  }
}

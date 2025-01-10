package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionTypeDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionTypeFaker.buildDebtPositionType;
import static it.gov.pagopa.pu.workflow.utils.faker.DebtPositionTypeFaker.buildDebtPositionTypeRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeMapperTest {

  @InjectMocks
  private final DebtPositionTypeMapper mapper = Mappers.getMapper(DebtPositionTypeMapper.class);

  @Test
  void testMapDebtPositionType() {
    DebtPositionTypeDTO expected = buildDebtPositionType();

    DebtPositionTypeDTO result = mapper.map(buildDebtPositionTypeRequestDTO());

    assertEquals(expected, result);
    checkNotNullFields(result);
  }

}

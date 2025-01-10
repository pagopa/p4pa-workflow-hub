package it.gov.pagopa.pu.workflow.wf.debtposition.mapper;

import it.gov.pagopa.payhub.activities.dto.debtposition.InstallmentDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.workflow.utils.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.workflow.utils.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.InstallmentFaker.buildInstallmentRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.PersonFaker.buildPersonDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.PersonFaker.buildPersonRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.ReceiptFaker.buildReceiptDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.ReceiptFaker.buildReceiptRequestDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.TransferFaker.buildTransferDTO;
import static it.gov.pagopa.pu.workflow.utils.faker.TransferFaker.buildTransferRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class InstallmentMapperTest {

  @Mock
  private TransferMapper transferMapperMock;
  @Mock
  private ReceiptMapper receiptMapperMock;
  @Mock
  private PersonMapper personMapperMock;

  @InjectMocks
  private final InstallmentMapper mapper = Mappers.getMapper(InstallmentMapper.class);

  @Test
  void testMapInstallmentDTO() {
    InstallmentDTO expected = buildInstallmentDTO();
    Mockito.when(transferMapperMock.map(buildTransferRequestDTO())).thenReturn(buildTransferDTO());

    Mockito.when(receiptMapperMock.map(buildReceiptRequestDTO())).thenReturn(buildReceiptDTO());

    Mockito.when(personMapperMock.map(buildPersonRequestDTO())).thenReturn(buildPersonDTO());

    InstallmentDTO installment = mapper.map(buildInstallmentRequestDTO());

    checkNotNullFields(installment);
    assertEquals(expected, installment);
  }
}

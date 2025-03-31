package it.gov.pagopa.pu.workflow.wf.classification.iuf.activity;

import it.gov.pagopa.pu.workflow.wf.classification.transfer.TransferClassificationWFClient;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.dto.TransferClassificationStartSignalDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StartTransferClassificationActivityTest {
  @Mock
  private TransferClassificationWFClient transferClassificationWFClient;

  private StartTransferClassificationActivity startTransferClassificationActivity;

  @BeforeEach
  void init() {
    startTransferClassificationActivity = new StartTransferClassificationActivityImpl(transferClassificationWFClient);
  }

  @Test
  void testSignalTransferClassificationWithStart() {
    assertDoesNotThrow(() -> startTransferClassificationActivity.signalTransferClassificationWithStart(1L, "iuv", "iur", 1));
    TransferClassificationStartSignalDTO expectedSignalDTO = TransferClassificationStartSignalDTO.builder()
      .orgId(1L)
      .iuv("iuv")
      .iur("iur")
      .transferIndex(1)
      .build();
    verify(transferClassificationWFClient).startTransferClassification(expectedSignalDTO);
  }
}

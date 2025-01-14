package it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
public class TransferClassificationWfHelperActivityTest {
  private TransferClassificationWfHelperActivity transferClassificationWfHelperActivity;

  @BeforeEach
  void init() {
    transferClassificationWfHelperActivity = new TransferClassificationWfHelperActivityImpl();
  }

  @Test
  void testSignalTransferClassificationWithStart() {
    assertDoesNotThrow(() -> transferClassificationWfHelperActivity.signalTransferClassificationWithStart(1L, "iuv", "iur", 1));
  }
}

package it.gov.pagopa.pu.workflow.wf.classification.iuf.activity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
public class StartTransferClassificationActivityTest {
  private StartTransferClassificationActivity startTransferClassificationActivity;

  @BeforeEach
  void init() {
    startTransferClassificationActivity = new StartTransferClassificationActivityImpl();
  }

  @Test
  void testSignalTransferClassificationWithStart() {
    assertDoesNotThrow(() -> startTransferClassificationActivity.signalTransferClassificationWithStart(1L, "iuv", "iur", 1));
  }
}

package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.mapper;

import it.gov.pagopa.payhub.activities.dto.IONotificationMessage;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FineWfExecutionConfigMapperTest {

  @Test
  void testMapNotifiedInstallment() {
    IONotificationMessage ioNotificationMessage = new IONotificationMessage("subject", "message");
    FineWfExecutionConfig fineConfig = FineWfExecutionConfig.builder()
      .ioMessages(new FineWfExecutionConfig.IONotificationFineWfMessages(ioNotificationMessage, null))
      .build();

    GenericWfExecutionConfig result = FineWfExecutionConfigMapper.mapNotifiedInstallment(fineConfig);

    assertNotNull(result);
    assertEquals(ioNotificationMessage, result.getIoMessages().getCreated());
    assertEquals(ioNotificationMessage, result.getIoMessages().getUpdated());
  }

  @Test
  void testMapReductionExpired() {
    IONotificationMessage ioNotificationMessage = new IONotificationMessage("expired-subject", "expired-message");
    FineWfExecutionConfig.IONotificationFineWfMessages fineMessages =
      new FineWfExecutionConfig.IONotificationFineWfMessages(null, ioNotificationMessage);

    FineWfExecutionConfig fineConfig = new FineWfExecutionConfig();
    fineConfig.setIoMessages(fineMessages);

    GenericWfExecutionConfig result = FineWfExecutionConfigMapper.mapReductionExpired(fineConfig);

    assertNotNull(result);
    assertEquals(ioNotificationMessage, result.getIoMessages().getCreated());
    assertEquals(ioNotificationMessage, result.getIoMessages().getUpdated());
  }
}


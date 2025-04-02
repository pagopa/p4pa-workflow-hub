package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.mapper;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;

public class FineWfExecutionConfigMapper {

  private FineWfExecutionConfigMapper() {}

  public static GenericWfExecutionConfig mapNotifiedInstallment(FineWfExecutionConfig fineConfig) {
    return GenericWfExecutionConfig.builder()
      .ioMessages(GenericWfExecutionConfig.IONotificationBaseOpsMessages.builder()
        .created(fineConfig.getIoMessages().getNotified())
        .updated(fineConfig.getIoMessages().getNotified())
        .build())
      .build();
  }

  public static GenericWfExecutionConfig mapReductionExpired(FineWfExecutionConfig fineConfig) {
    return GenericWfExecutionConfig.builder()
      .ioMessages(GenericWfExecutionConfig.IONotificationBaseOpsMessages.builder()
        .created(fineConfig.getIoMessages().getReductionExpired())
        .updated(fineConfig.getIoMessages().getReductionExpired())
        .build())
      .build();
  }
}

package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.mapper;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;

public class FineWfExecutionConfigMapper {

  private FineWfExecutionConfigMapper() {}

  public static GenericWfExecutionConfig mapFineWfExecutionConfigToGenericWfExecutionConfigForIsNotified(FineWfExecutionConfig fineConfig) {
    return GenericWfExecutionConfig.builder()
      .ioMessages(GenericWfExecutionConfig.IONotificationBaseOpsMessages.builder()
        .created(fineConfig.getIoMessages().getNotified())
        .build())
      .build();
  }

  public static GenericWfExecutionConfig mapFineWfExecutionConfigToGenericWfExecutionConfigForReductionExpired(FineWfExecutionConfig fineConfig) {
    return GenericWfExecutionConfig.builder()
      .ioMessages(GenericWfExecutionConfig.IONotificationBaseOpsMessages.builder()
        .created(fineConfig.getIoMessages().getReductionExpired())
        .build())
      .build();
  }
}

package it.gov.pagopa.pu.workflow.wf.debtposition.custom.fine.mapper;

import it.gov.pagopa.payhub.activities.dto.IONotificationMessage;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig;
import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.GenericWfExecutionConfig;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;

public class FineWfExecutionConfigMapper {

  private FineWfExecutionConfigMapper() {}

  public static GenericWfExecutionConfig mapNotifiedInstallment(FineWfExecutionConfig fineConfig, DebtPositionDTO debtPositionDTO) {
    IONotificationMessage originalMessage = fineConfig.getIoMessages().getNotified();
    return applyFinePlaceholders(debtPositionDTO, originalMessage);
  }

  public static GenericWfExecutionConfig mapReductionExpired(FineWfExecutionConfig fineConfig, DebtPositionDTO debtPositionDTO) {
    IONotificationMessage originalMessage = fineConfig.getIoMessages().getReductionExpired();
    return applyFinePlaceholders(debtPositionDTO, originalMessage);
  }

  private static GenericWfExecutionConfig applyFinePlaceholders(DebtPositionDTO debtPositionDTO, IONotificationMessage originalMessage) {
    String markdown = IoNotificationFinePlaceholderResolver.applyFinePlaceholder(originalMessage.getMessage(), debtPositionDTO);

    IONotificationMessage resolvedNotification = new IONotificationMessage(
      originalMessage.getSubject(),
      markdown
    );

    return GenericWfExecutionConfig.builder()
      .ioMessages(GenericWfExecutionConfig.IONotificationBaseOpsMessages.builder()
        .created(resolvedNotification)
        .updated(resolvedNotification)
        .build())
      .build();
  }
}


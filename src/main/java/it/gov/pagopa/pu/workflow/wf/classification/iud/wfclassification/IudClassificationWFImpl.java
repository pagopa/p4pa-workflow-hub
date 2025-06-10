package it.gov.pagopa.pu.workflow.wf.classification.iud.wfclassification;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.classifications.ClearClassifyIudActivity;
import it.gov.pagopa.payhub.activities.activity.classifications.IudClassificationActivity;
import it.gov.pagopa.payhub.activities.dto.classifications.IudClassificationActivityResult;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowServiceImpl;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.classification.iud.config.IudClassificationWfConfig;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyPaymentNotificationSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyReceiptSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.activity.StartTransferClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.dto.TransferClassificationStartSignalDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_CLASSIFICATION_MEDIUM_PRIORITY)
public class IudClassificationWFImpl implements IudClassificationWF, ApplicationContextAware {

  private ClearClassifyIudActivity clearClassifyIudActivity;
  private IudClassificationActivity iudClassificationActivity;

  private StartTransferClassificationActivity startTransferClassificationActivity;

  private final Queue<TransferClassificationStartSignalDTO> toNotify = new ConcurrentLinkedQueue<>();

  /**
   * Temporal workflow will not allow to use injection in order to avoid
   * <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    IudClassificationWfConfig wfConfig = applicationContext.getBean(IudClassificationWfConfig.class);

    clearClassifyIudActivity = wfConfig.buildClearClassifyIudActivityStub();
    iudClassificationActivity = wfConfig.buildIudClassificationActivityStub();

    startTransferClassificationActivity = wfConfig.buildStartTransferClassificationActivityStub();
  }

  @Override
  public void classify() {
    WorkflowServiceImpl.waitForSignalMethods();
    log.info("Notifying Transfer Classifications: {}", toNotify);

    toNotify.forEach(transferClassificationStartSignalDTO ->
      startTransferClassificationActivity.signalTransferClassificationWithStart(
        transferClassificationStartSignalDTO.getOrgId(),
        transferClassificationStartSignalDTO.getIuv(),
        transferClassificationStartSignalDTO.getIur(),
        transferClassificationStartSignalDTO.getTransferIndex()));
  }

  @Override
  public void notifyReceipt(IudClassificationNotifyReceiptSignalDTO signalDTO) {
    deleteClassification(signalDTO.getOrganizationId(), signalDTO.getIud());

    addToNotifyQueue(signalDTO.getOrganizationId(),
      signalDTO.getIuv(),
      signalDTO.getIur(),
      signalDTO.getTransferIndexes());
  }

  @Override
  public void notifyPaymentNotification(IudClassificationNotifyPaymentNotificationSignalDTO signalDTO) {
    deleteClassification(signalDTO.getOrganizationId(), signalDTO.getIud());

    IudClassificationActivityResult activityResult = iudClassificationActivity.classify(signalDTO.getOrganizationId(), signalDTO.getIud());

    addToNotifyQueue(activityResult.getOrganizationId(),
      activityResult.getIuv(),
      activityResult.getIur(),
      activityResult.getTransferIndexes());
  }

  private void deleteClassification(Long organizationId, String iud) {
    log.info("Handling payment notification in iud classification for organization ID {} and iud {}", organizationId, iud);
    Long clearedResult = clearClassifyIudActivity.deleteClassificationByIud(
      organizationId, iud);
    log.info("IUD payment notification classification cleared {} records for organization ID {} and iud {}", clearedResult, organizationId, iud);
  }

  private void addToNotifyQueue(Long organizationId,
                                String iuv,
                                String iur,
                                List<Integer> transferIndexes) {
    transferIndexes.stream()
      .map(index -> TransferClassificationStartSignalDTO.builder()
        .orgId(organizationId)
        .iuv(iuv)
        .iur(iur)
        .transferIndex(index)
        .build())
      .forEach(toNotify::add);
  }
}

package it.gov.pagopa.pu.workflow.wf.classification.iud.wfclassification;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.classifications.ClearClassifyIudActivity;
import it.gov.pagopa.payhub.activities.activity.classifications.IudClassificationActivity;
import it.gov.pagopa.pu.workflow.service.WorkflowServiceImpl;
import it.gov.pagopa.pu.workflow.wf.classification.iud.config.IudClassificationWfConfig;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyPaymentNotificationSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iud.dto.IudClassificationNotifyReceiptSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.dto.TransferClassificationStartSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.activity.StartTransferClassificationActivity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static it.gov.pagopa.pu.workflow.wf.classification.iud.wfclassification.IudClassificationWFImpl.TASK_QUEUE_IUF_CLASSIFICATION_WF;

@Slf4j
@WorkflowImpl(taskQueues = TASK_QUEUE_IUF_CLASSIFICATION_WF)
public class IudClassificationWFImpl implements IudClassificationWF, ApplicationContextAware {
  public static final String TASK_QUEUE_IUF_CLASSIFICATION_WF = "IudClassificationWF";
  public static final String TASK_QUEUE_IUD_CLASSIFICATION_LOCAL_ACTIVITY = "IudClassificationWF_LOCAL";

  private ClearClassifyIudActivity clearClassifyIudActivity;
  private IudClassificationActivity iudClassificationActivity;

  private StartTransferClassificationActivity startTransferClassificationActivity;

  private final Queue<TransferClassificationStartSignalDTO> toNotify = new ConcurrentLinkedQueue<>();

  /**
   * Temporal workflow will not allow to use injection in order to avoid
   * <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
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
    log.info("Handling receipt notification in iud classification: {}", signalDTO);
    Long clearedResult = clearClassifyIudActivity.deleteClassificationByIud(
      signalDTO.getOrgId(),
      signalDTO.getIud());
    log.info("IUD receipt classification cleared {} records for {}", clearedResult, signalDTO);

    TransferClassificationStartSignalDTO transferClassificationStartSignalDTO = TransferClassificationStartSignalDTO.builder()
      .orgId(signalDTO.getOrgId())
      .iuv(signalDTO.getIuv())
      .iur(signalDTO.getIur())
      .transferIndex(signalDTO.getTransferIndex())
      .build();
    toNotify.add(transferClassificationStartSignalDTO);
  }

  @Override
  public void notifyPaymentNotification(IudClassificationNotifyPaymentNotificationSignalDTO signalDTO) {

    log.info("Handling payment notification in iud classification: {}", signalDTO);
    Long clearedResult = clearClassifyIudActivity.deleteClassificationByIud(
      signalDTO.getOrganizationId(),
      signalDTO.getIud());
    log.info("IUD payment notification classification cleared {} records for {}", clearedResult, signalDTO);

    iudClassificationActivity.classify("signalDTO.getOrganizationId()", signalDTO.getIud());

    TransferClassificationStartSignalDTO transferClassificationStartSignalDTO = TransferClassificationStartSignalDTO.builder()
      .build();
    toNotify.add(transferClassificationStartSignalDTO);
  }
}

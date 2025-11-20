package it.gov.pagopa.pu.workflow.wf.classification.transfer.wfclassification;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.classifications.TransferClassificationActivity;
import it.gov.pagopa.payhub.activities.dto.classifications.TransferClassifyDTO;
import it.gov.pagopa.payhub.activities.dto.classifications.TransferSemanticKeyDTO;
import it.gov.pagopa.pu.debtposition.dto.generated.InstallmentNoPII;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowServiceImpl;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.classification.assessments.activity.StartAssessmentClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.config.TransferClassificationWfConfig;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.dto.TransferClassificationStartSignalDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_CLASSIFICATION_MEDIUM_PRIORITY)
public class TransferClassificationWFImpl implements TransferClassificationWF, ApplicationContextAware {

  private TransferClassificationActivity transferClassificationActivity;
  private StartAssessmentClassificationActivity startAssessmentClassificationActivity;

  private final Collection<TransferSemanticKeyDTO> toClassify = new ConcurrentLinkedQueue<>();

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    TransferClassificationWfConfig wfConfig = applicationContext.getBean(TransferClassificationWfConfig.class);
    transferClassificationActivity = wfConfig.buildTransferClassificationActivityStub();
    startAssessmentClassificationActivity = wfConfig.buildStartAssessmentClassificationActivityStub();
  }

  @Override
  public void classify() {
    WorkflowServiceImpl.waitForSignalMethods();

    log.info("Classifying Transfers: {}", toClassify);

    toClassify.stream().distinct()
      .forEach(item -> {
        log.info("Handling Transfer classification for semantic key {}", item);
        TransferClassifyDTO classifiedResult = transferClassificationActivity.classifyTransfer(item);
        if(classifiedResult!=null) {
          InstallmentNoPII installmentNoPII = classifiedResult.getInstallmentNoPII();
          if(installmentNoPII != null) {
            log.info("Handling Assessment classification for semantic key {}", item);
            startAssessmentClassificationActivity.signalAssessmentClassificationWithStart(item.getOrgId(),
              classifiedResult.getInstallmentNoPII().getIuv(),
              classifiedResult.getInstallmentNoPII().getIud());
          }
        }
        log.info("Ingestion to classify Transfers with semantic key {} is completed", item);
      });
  }

  @Override
  public void startTransferClassification(TransferClassificationStartSignalDTO signalDTO) {
    log.info("Starting Transfer classification with signal {}", signalDTO);
    TransferSemanticKeyDTO transferSemanticKeyDTO = TransferSemanticKeyDTO.builder()
      .orgId(signalDTO.getOrgId())
      .iuv(signalDTO.getIuv())
      .iur(signalDTO.getIur())
      .transferIndex(signalDTO.getTransferIndex())
      .build();
    toClassify.add(transferSemanticKeyDTO);
  }
}

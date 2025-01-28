package it.gov.pagopa.pu.workflow.wf.classification.transfer.wfclassification;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.classifications.TransferClassificationActivity;
import it.gov.pagopa.payhub.activities.dto.classifications.TransferSemanticKeyDTO;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.config.TransferClassificationWfConfig;
import it.gov.pagopa.pu.workflow.wf.classification.transfer.dto.TransferClassificationStartSignalDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.*;

import static it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion.PaymentsReportingIngestionWFImpl.TASK_QUEUE;

@Slf4j
@WorkflowImpl(taskQueues = TASK_QUEUE)
public class TransferClassificationWFImpl implements TransferClassificationWF, ApplicationContextAware {
  public static final String TASK_QUEUE = "TransferClassificationWF";

  private TransferClassificationActivity transferClassificationActivity;

  private List<TransferSemanticKeyDTO> toClassify = new ArrayList<>();

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    TransferClassificationWfConfig wfConfig = applicationContext.getBean(TransferClassificationWfConfig.class);
    transferClassificationActivity = wfConfig.buildTransferClassificationActivityStub();
  }

  @Override
  public void classify() {
    toClassify.stream().distinct()
      .forEach(item -> {
        log.info("Handling Transfer classification for semantic key", item);
        transferClassificationActivity.classify(item);
      });
    log.info("Ingestion to classify Transfers is completed");
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

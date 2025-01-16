package it.gov.pagopa.pu.workflow.wf.classification.iuf.classification;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.classifications.*;
import it.gov.pagopa.payhub.activities.dto.classifications.IufClassificationActivityResult;
import it.gov.pagopa.payhub.activities.dto.classifications.Transfer2ClassifyDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyPaymentsReportingSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyTreasurySignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.config.IufClassificationWfConfig;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.activity.StartTransferClassificationActivity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion.PaymentsReportingIngestionWFImpl.TASK_QUEUE;

/**
 * Implementation of the IufClassificationWF interface
 */
@Slf4j
@WorkflowImpl(taskQueues = TASK_QUEUE)
public class IufClassificationWFImpl implements IufClassificationWF, ApplicationContextAware {
  public static final String TASK_QUEUE = "IufClassificationWF";

  private ClearClassifyIufActivity clearClassifyIufActivity;
  private IufClassificationActivity iufClassificationActivity;

  private StartTransferClassificationActivity startTransferClassificationActivity;

  List<IufClassificationActivityResult> toNotify = new ArrayList<>();;

  /**
   * Temporal workflow will not allow to use injection in order to avoid
   * <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    IufClassificationWfConfig wfConfig = applicationContext.getBean(IufClassificationWfConfig.class);

    clearClassifyIufActivity = wfConfig.buildClearClassifyIufActivityStub();
    iufClassificationActivity = wfConfig.buildIufClassificationActivityStub();

    startTransferClassificationActivity = wfConfig.buildStartTransferClassificationActivityStub();

  }

  @Override
  public void classify() {

    toNotify.stream()
      .flatMap(r -> r. getTransfers2classify().stream())
      .distinct()
      .forEach(transfer2ClassifyDTO -> {
      String iuv = transfer2ClassifyDTO.getIuv();
      String iur = transfer2ClassifyDTO.getIur();
      int transferIndex = transfer2ClassifyDTO.getTransferIndex();
      startTransferClassificationActivity.signalTransferClassificationWithStart(toNotify.get(0).getOrganizationId(), iuv, iur, transferIndex);
    });

  }


  @Override
  public void notifyTreasury(IufClassificationNotifyTreasurySignalDTO signalDTO) {

    log.info("Handling treasury notification in iuf classification: {}", signalDTO);
    boolean clearedResult = clearClassifyIufActivity.deleteClassificationByIuf(
      signalDTO.getOrganizationId(),
      signalDTO.getIuf());

    log.info("IUF receipt classification cleared with result {} for {}", clearedResult, signalDTO);

    IufClassificationActivityResult iufClassificationActivityResult = iufClassificationActivity.classify(signalDTO.getOrganizationId(), signalDTO.getTreasuryId(), signalDTO.getIuf());
    toNotify.add(iufClassificationActivityResult);
  }

  @Override
  public void notifyPaymentsReporting(IufClassificationNotifyPaymentsReportingSignalDTO signalDTO) {

    log.info("Handling payments reporting notification in iuf classification: {}", signalDTO);
    boolean clearedResult = clearClassifyIufActivity.deleteClassificationByIuf(
      signalDTO.getOrganizationId(),
      signalDTO.getIuf());

    log.info("IUF receipt classification cleared with result {} for {}", clearedResult, signalDTO);

    toNotify.add(IufClassificationActivityResult.builder()
      .organizationId(signalDTO.getOrganizationId())
      .success(true)
      .transfers2classify(signalDTO.getTransfers2classify())
      .build());
  }

}

package it.gov.pagopa.pu.workflow.wf.classification.iuf.classification;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.classifications.ClearClassifyIufActivity;
import it.gov.pagopa.payhub.activities.activity.classifications.IufClassificationActivity;
import it.gov.pagopa.payhub.activities.dto.classifications.IufClassificationActivityResult;
import it.gov.pagopa.payhub.activities.dto.classifications.Transfer2ClassifyDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.activity.StartTransferClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.config.IufClassificationWfConfig;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyPaymentsReportingSignalDTO;
import it.gov.pagopa.pu.workflow.wf.classification.iuf.dto.IufClassificationNotifyTreasurySignalDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;

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

  List<IufClassificationActivityResult> toNotify = new ArrayList<>();

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
      startTransferClassificationActivity.signalTransferClassificationWithStart(toNotify.getFirst().getOrganizationId(), iuv, iur, transferIndex);
    });

  }


  @Override
  public void notifyTreasury(IufClassificationNotifyTreasurySignalDTO signalDTO) {

    log.info("Handling treasury notification in iuf classification: {}", signalDTO);
    Long clearedResult = clearClassifyIufActivity.deleteClassificationByIuf(
      signalDTO.getOrganizationId(),
      signalDTO.getIuf());

    log.info("IUF receipt classification cleared {} records for {}", clearedResult, signalDTO);

    IufClassificationActivityResult iufClassificationActivityResult = iufClassificationActivity.classify(signalDTO.getOrganizationId(), signalDTO.getTreasuryId(), signalDTO.getIuf());
    toNotify.add(iufClassificationActivityResult);
  }

  @Override
  public void notifyPaymentsReporting(IufClassificationNotifyPaymentsReportingSignalDTO signalDTO) {
    log.info("Handling payments reporting notification in iuf classification: {}", signalDTO);
    Long clearedResult = clearClassifyIufActivity.deleteClassificationByIuf(
      signalDTO.getOrganizationId(),
      signalDTO.getIuf());

    log.info("IUF receipt classification cleared cleared {} records for {}", clearedResult, signalDTO);
    List<Transfer2ClassifyDTO> transfer2ClassifyDTOList = signalDTO.getTransfers().stream()
      .map(transfer -> Transfer2ClassifyDTO.builder()
        .iur(transfer.getIur())
        .iuv(transfer.getIuv())
        .transferIndex(transfer.getTransferIndex())
        .build()
      ).toList();

    toNotify.add(IufClassificationActivityResult.builder()
      .organizationId(signalDTO.getOrganizationId())
      .transfers2classify(transfer2ClassifyDTOList)
      .build());
  }

}

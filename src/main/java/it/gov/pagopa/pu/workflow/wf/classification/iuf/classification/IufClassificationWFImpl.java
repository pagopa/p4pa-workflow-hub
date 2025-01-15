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

import java.util.HashSet;
import java.util.Set;

import static it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion.PaymentsReportingIngestionWFImpl.TASK_QUEUE;

/**
 * Implementation of the IufClassificationWF interface
 */
@Slf4j
@WorkflowImpl(taskQueues = TASK_QUEUE)
public class IufClassificationWFImpl implements IufClassificationWF, ApplicationContextAware {
  public static final String TASK_QUEUE = "IufClassificationWF";

  private IufClassificationNotifyTreasurySignalDTO signalDTO;

  private ClearClassifyIufActivity clearClassifyIufActivity;
  private IufClassificationActivity iufClassificationActivity;
  private TransferClassificationActivity transferClassificationActivity;

  private StartTransferClassificationActivity startTransferClassificationActivity;

  private IufClassificationActivityResult result;

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

    startTransferClassificationActivity = wfConfig.buildTransferClassificationStarterHelperActivityStub();

  }

  @Override
  public void classify() {

    if (result != null && result.isSuccess()) {

      Set<Transfer2ClassifyDTO> uniqueTransfers = new HashSet<>(result.getTransfers2classify());

      // signal transfer classification for each transfer
      uniqueTransfers.forEach(transfer2ClassifyDTO -> {

        String iuv = transfer2ClassifyDTO.getIuv();
        String iur = transfer2ClassifyDTO.getIur();
        int transferIndex = transfer2ClassifyDTO.getTransferIndex();

        startTransferClassificationActivity.signalTransferClassificationWithStart(result.getOrganizationId(), iuv, iur, transferIndex);
      });

    } else {
      log.warn("Result is null or classification was not successful for organizationId {}",
        result != null ? result.getOrganizationId() : "N/A");
    }

  }


  @Override
  public void notifyTreasury(IufClassificationNotifyTreasurySignalDTO signalDTO) {

    log.info("Handling treasury notification in iuf classification: {}", signalDTO);
    boolean clearedResult = clearClassifyIufActivity.deleteClassificationByIuf(
      signalDTO.getOrganizationId(),
      signalDTO.getIuf());

    log.info("IUF receipt classification cleared with result {} for {}", clearedResult, signalDTO);

    IufClassificationActivityResult iufClassificationActivityResult = iufClassificationActivity.classify(signalDTO.getOrganizationId(), signalDTO.getTreasuryId(), signalDTO.getIuf());

    if (iufClassificationActivityResult.isSuccess()) {
      if (result == null) {
        result = iufClassificationActivityResult;
      } else {
        Set<Transfer2ClassifyDTO> uniqueTransfers = new HashSet<>(result.getTransfers2classify());
        uniqueTransfers.addAll(iufClassificationActivityResult.getTransfers2classify());
        result.getTransfers2classify().clear();
        result.getTransfers2classify().addAll(uniqueTransfers);
      }
    } else {
      log.error("Error in treasury classification for organizationId {}", signalDTO.getOrganizationId());
    }
  }

  @Override
  public void notifyPaymentsReporting(IufClassificationNotifyPaymentsReportingSignalDTO signalDTO) {

    log.info("Handling payments reporting notification in iuf classification: {}", signalDTO);
    boolean clearedResult = clearClassifyIufActivity.deleteClassificationByIuf(
      signalDTO.getOrganizationId(),
      signalDTO.getIuf());

    log.info("IUF receipt classification cleared with result {} for {}", clearedResult, signalDTO);

    if (result == null) {
      result = IufClassificationActivityResult.builder()
        .organizationId(signalDTO.getOrganizationId())
        .success(true)
        .transfers2classify(signalDTO.getTransfers2classify())
        .build();
    } else {

      Set<Transfer2ClassifyDTO> uniqueTransfers = new HashSet<>(result.getTransfers2classify());
      uniqueTransfers.addAll(signalDTO.getTransfers2classify());

      result.getTransfers2classify().clear();
      result.getTransfers2classify().addAll(uniqueTransfers);

    }
  }

}

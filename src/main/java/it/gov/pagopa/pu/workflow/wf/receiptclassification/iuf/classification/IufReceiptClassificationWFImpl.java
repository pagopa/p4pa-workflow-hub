package it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.classification;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.classifications.*;
import it.gov.pagopa.payhub.activities.dto.classifications.IufClassificationActivityResult;
import it.gov.pagopa.payhub.activities.dto.classifications.Transfer2ClassifyDTO;
import it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.dto.IufReceiptClassificationForReportingSignalDTO;
import it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.dto.IufReceiptClassificationForTreasurySignalDTO;
import it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.config.IufReceiptClassificationWfConfig;
import it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.helper.TransferClassificationWfHelperActivity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion.PaymentsReportingIngestionWFImpl.TASK_QUEUE;

/**
 * Implementation of the IufReceiptClassificationWF interface
 */
@Slf4j
@WorkflowImpl(taskQueues = TASK_QUEUE)
public class IufReceiptClassificationWFImpl implements IufReceiptClassificationWF, ApplicationContextAware {
  public static final String TASK_QUEUE = "IufReceiptClassificationWF";

  private IufReceiptClassificationForTreasurySignalDTO signalDTO;

  private ClearClassifyIufActivity clearClassifyIufActivity;
  private IufClassificationActivity iufClassificationActivity;
  private TransferClassificationActivity transferClassificationActivity;

  private TransferClassificationWfHelperActivity transferClassificationWfHelperActivity;

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
    IufReceiptClassificationWfConfig wfConfig = applicationContext.getBean(IufReceiptClassificationWfConfig.class);

    clearClassifyIufActivity = wfConfig.buildClearClassifyIufActivityStub();
    iufClassificationActivity = wfConfig.buildIufClassificationActivityStub();
    transferClassificationActivity = wfConfig.buildTransferClassificationActivityStub();

    transferClassificationWfHelperActivity = wfConfig.buildTransferClassificationStarterHelperActivityStub();

  }

  @Override
  public void classify() {

    if (result != null && result.isSuccess()) {

      // signal transfer classification for each transfer
      result.getTransfers2classify().forEach(transfer2ClassifyDTO -> {

        String iuv = transfer2ClassifyDTO.getIuv();
        String iur = transfer2ClassifyDTO.getIur();
        int transferIndex = transfer2ClassifyDTO.getTransferIndex();

        transferClassificationWfHelperActivity.signalTransferClassificationWithStart(result.getOrganizationId(), iuv, iur, transferIndex);
      });

    } else {
      log.warn("Result is null or classification was not successful for organizationId {}, treasuryId {} and iuf {}",
        result != null ? result.getOrganizationId() : "N/A",
        signalDTO.getTreasuryId(),
        signalDTO.getIuf());
    }

    log.info("IUF receipt classification completed for for for organizationId {}, treasuryId {} and iuf {}",
      result.getOrganizationId(), signalDTO.getTreasuryId(), signalDTO.getIuf());
  }


  @Override
  public void signalForTreasury(IufReceiptClassificationForTreasurySignalDTO signalDTO) {

    log.info("Handling iuf receipt classification for treasury with organizationId {}, treasuryId {} and IUF {}",
      signalDTO.getOrganizationId(), signalDTO.getTreasuryId(), signalDTO.getIuf());

    boolean clearedForTreasury = clearClassifyIufActivity.deleteClassificationByIuf(
      signalDTO.getOrganizationId(),
      signalDTO.getIuf());

    log.info("IUF receipt classification cleared with result {} for organizationId {}, treasuryId {} and IUF {}",
      clearedForTreasury,
      signalDTO.getOrganizationId(), signalDTO.getTreasuryId(), signalDTO.getIuf());

    result = iufClassificationActivity.classify(signalDTO.getOrganizationId(), signalDTO.getTreasuryId(), signalDTO.getIuf());

  }

  @Override
  public void signalForReporting(IufReceiptClassificationForReportingSignalDTO signalDTO) {

    log.info("Handling iuf receipt classification for reporting with organizationId {}, IUF {} and outcome {}",
      signalDTO.getOrganizationId(),
      signalDTO.getIuf(),
      signalDTO.getOutcomeCode()
    );

    boolean clearedForTreasury = clearClassifyIufActivity.deleteClassificationByIuf(
      signalDTO.getOrganizationId(),
      signalDTO.getIuf());

    log.info("IUF receipt classification for reporting cleared. Result is {} for organizationId {}, iuf {} and outcome {}",
      clearedForTreasury,
      signalDTO.getOrganizationId(),
      signalDTO.getIuf(),
      signalDTO.getOutcomeCode());

    result = IufClassificationActivityResult.builder()
      .organizationId(signalDTO.getOrganizationId())
      .success(true)
      .transfers2classify(signalDTO.getTransfers2classify())
      .build();

  }

}

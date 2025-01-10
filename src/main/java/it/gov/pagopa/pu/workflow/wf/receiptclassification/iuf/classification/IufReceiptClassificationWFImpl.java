package it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.classification;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.classifications.*;
import it.gov.pagopa.payhub.activities.dto.classifications.IufClassificationActivityResult;
import it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.IufReceiptClassificationSignalType;
import it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.IufReceiptClassificationSignalDTO;
import it.gov.pagopa.pu.workflow.wf.receiptclassification.iuf.config.IufReceiptClassificationWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion.PaymentsReportingIngestionWFImpl.TASK_QUEUE;

/**
 *
 */
@Slf4j
@WorkflowImpl(taskQueues = TASK_QUEUE)
public class IufReceiptClassificationWFImpl implements IufReceiptClassificationWF, ApplicationContextAware {
  public static final String TASK_QUEUE = "IufReceiptClassificationWF";

  private IufReceiptClassificationSignalDTO signalDTO;

  private ClearClassifyIufActivity clearClassifyIufActivity;
  private IufClassificationActivity iufClassificationActivity;
  private TransferClassificationActivity transferClassificationActivity;


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

  }

  @Override
  public void classify(IufReceiptClassificationSignalDTO signalDTO) {

    switch (signalDTO.getType()) {
      case IufReceiptClassificationSignalType.RENDICONTAZIONE:


        log.info("Handling iuf receipt classification for organizatioId {}, treasuryId {} and iuf {}",
          signalDTO.getOrganizationId(), signalDTO.getTreasuryId(), signalDTO.getIuf());

        boolean clearedForTreasury = clearClassifyIufActivity.deleteClassificationByIuf(signalDTO.getOrganizationId(), signalDTO.getIuf());

        log.info("IUF receipt classification cleared with result {} for organizatioId {}, treasuryId {} and iuf {}",
          clearedForTreasury,
          signalDTO.getOrganizationId(), signalDTO.getTreasuryId(), signalDTO.getIuf());

        IufClassificationActivityResult result = iufClassificationActivity.classify(signalDTO.getOrganizationId(), signalDTO.getTreasuryId(), signalDTO.getIuf());







        log.info("IUF receipt classification completed for for for organizatioId {}, treasuryId {} and iuf {}",
          signalDTO.getOrganizationId(), signalDTO.getTreasuryId(), signalDTO.getIuf());
        break;


      case IufReceiptClassificationSignalType.TESORERIA:

        // TODO: implement the logic

        log.info("Handling iuf receipt classification for organizatioId {}, treasuryId {} and iuf {}",
          signalDTO.getOrganizationId(), signalDTO.getTreasuryId(), signalDTO.getIuf());

        boolean clearedForReporting = clearClassifyIufActivity.deleteClassificationByIuf(signalDTO.getOrganizationId(), signalDTO.getIuf());

        log.info("IUF receipt classification cleared with result {} for organizatioId {}, treasuryId {} and iuf {}",
          clearedForReporting,
          signalDTO.getOrganizationId(), signalDTO.getTreasuryId(), signalDTO.getIuf());


        break;
      default:
        throw new IllegalArgumentException("Invalid classification type: " + signalDTO.getType());

    }
  }

  @Override
  public void setSignalDTO(IufReceiptClassificationSignalDTO signalDTO) {
    signalDTO = signalDTO;
  }

}

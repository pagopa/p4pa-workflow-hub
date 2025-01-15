package it.gov.pagopa.pu.workflow.wf.classifications.transfer.wfclassification;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.classifications.TransferClassificationActivity;
import it.gov.pagopa.pu.workflow.wf.classifications.transfer.config.TransferClassificationWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static it.gov.pagopa.pu.workflow.wf.ingestionflow.paymentsreporting.wfingestion.PaymentsReportingIngestionWFImpl.TASK_QUEUE;

@Slf4j
@WorkflowImpl(taskQueues = TASK_QUEUE)
public class TransferClassificationWFImpl implements TransferClassificationWF, ApplicationContextAware {
    public static final String TASK_QUEUE = "TransferClassificationWF";

    private TransferClassificationActivity transferClassificationActivity;

	public TransferClassificationWFImpl(TransferClassificationActivity transferClassificationActivity) {
		this.transferClassificationActivity = transferClassificationActivity;
	}

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
    public void classify(Long orgId, String iuv, String iur, int transferIndex) {
        log.info("Handling Transfer classification for organization id: {} and iuv: {} and iur {} and transfer index: {}", orgId, iuv, iur, transferIndex);
        transferClassificationActivity.classify(orgId, iuv, iur, transferIndex);
        log.info("Ingestion organization id {} and iuv {} and iur {} and transfer index {} is completed", orgId, iuv, iur, transferIndex);
    }
}

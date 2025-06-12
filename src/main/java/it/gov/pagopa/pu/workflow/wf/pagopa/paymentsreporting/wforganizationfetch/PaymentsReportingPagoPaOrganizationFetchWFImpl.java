package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wforganizationfetch;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.paymentsreporting.OrganizationPaymentsReportingPagoPaListRetrieverActivity;
import it.gov.pagopa.payhub.activities.activity.paymentsreporting.OrganizationPaymentsReportingPagoPaRetrieverActivity;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.config.OrganizationPaymentsReportingPagoPaFetchWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY)
public class PaymentsReportingPagoPaOrganizationFetchWFImpl implements PaymentsReportingPagoPaOrganizationFetchWF, ApplicationContextAware {

  private OrganizationPaymentsReportingPagoPaListRetrieverActivity organizationPaymentsReportingPagoPaListRetrieverActivity;
  private OrganizationPaymentsReportingPagoPaRetrieverActivity organizationPaymentsReportingPagoPaRetrieverActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    OrganizationPaymentsReportingPagoPaFetchWfConfig wfConfig = applicationContext.getBean(OrganizationPaymentsReportingPagoPaFetchWfConfig.class);

    organizationPaymentsReportingPagoPaListRetrieverActivity = wfConfig.buildOrganizationPaymentsReportingPagoPaListRetrieverActivityStub();
    organizationPaymentsReportingPagoPaRetrieverActivity = wfConfig.buildOrganizationPaymentsReportingPagoPaRetrieverActivityStub();
  }

  @Override
  public void retrieve(Long organizationId) {
    log.info("Fetching new PaymentsReporting for organizationId {} from PagoPA", organizationId);

    List<PaymentsReportingIdDTO> paymentsReportingIds = organizationPaymentsReportingPagoPaListRetrieverActivity.retrieveNotImportedPagoPaPaymentsReportingIds(organizationId);
    log.info("PagoPA PaymentsReporting retrieved for organization with ID {} are: {}", organizationId,
      String.join(", ", paymentsReportingIds.stream().map(PaymentsReportingIdDTO::getPaymentsReportingFileName).toList()));

    if (paymentsReportingIds.isEmpty()) {
      log.info("Skip OrganizationPaymentsReportingPagoPaRetrieverActivity - nothing new to fetch from PagoPA for the organization with ID {}", organizationId);
    } else {
      organizationPaymentsReportingPagoPaRetrieverActivity.fetchPagoPaPaymentsReportingFiles(organizationId, paymentsReportingIds);
    }

    log.info("Fetch of PaymentsReporting completed for organization with ID {} from PagoPA", organizationId);
  }
}

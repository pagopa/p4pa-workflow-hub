package it.gov.pagopa.pu.workflow.wf.pagopa.paymentreporting.wffetch;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.paymentsreporting.OrganizationPaymentsReportingPagoPaListRetrieverActivity;
import it.gov.pagopa.payhub.activities.activity.paymentsreporting.OrganizationPaymentsReportingPagoPaRetrieverActivity;
import it.gov.pagopa.pu.pagopapayments.dto.generated.PaymentsReportingIdDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentreporting.config.PaymentsReportingPagoPaWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

@Slf4j
@WorkflowImpl(taskQueues = OrganizationPaymentsReportingPagoPaFetchWFImpl.TASK_QUEUE_ORGANIZATION_PAYMENTS_REPORTING_PAGOPA_FETCH)
public class OrganizationPaymentsReportingPagoPaFetchWFImpl implements OrganizationPaymentsReportingPagoPaFetchWF, ApplicationContextAware {
  public static final String TASK_QUEUE_ORGANIZATION_PAYMENTS_REPORTING_PAGOPA_FETCH = "OrganizationPaymentsReportingPagoPaFetchWF";

  private OrganizationPaymentsReportingPagoPaListRetrieverActivity organizationPaymentsReportingPagoPaListRetrieverActivity;
  private OrganizationPaymentsReportingPagoPaRetrieverActivity organizationPaymentsReportingPagoPaRetrieverActivity;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    PaymentsReportingPagoPaWfConfig wfConfig = applicationContext.getBean(PaymentsReportingPagoPaWfConfig.class);

    organizationPaymentsReportingPagoPaListRetrieverActivity = wfConfig.buildOrganizationPaymentsReportingPagoPaListRetrieverActivityStub();
    organizationPaymentsReportingPagoPaRetrieverActivity = wfConfig.buildOrganizationPaymentsReportingPagoPaRetrieverActivityStub();
  }

  @Override
  public void retrieve(Long organizationId) {
    log.info("Handling PagoPA PaymentsReporting for organizationId {}", organizationId);

    List<PaymentsReportingIdDTO> paymentsReportingIds = organizationPaymentsReportingPagoPaListRetrieverActivity.retrieve(organizationId);
    log.info("PagoPA PaymentsReporting retrieved for organization with ID {} are: {}", organizationId,
      String.join(", ", paymentsReportingIds.stream().map(PaymentsReportingIdDTO::getPaymentsReportingFileName).toList()));
    organizationPaymentsReportingPagoPaRetrieverActivity.fetch(organizationId, paymentsReportingIds);
    log.info("PagoPA PaymentsReporting completed for organization with ID {}", organizationId);
  }
}

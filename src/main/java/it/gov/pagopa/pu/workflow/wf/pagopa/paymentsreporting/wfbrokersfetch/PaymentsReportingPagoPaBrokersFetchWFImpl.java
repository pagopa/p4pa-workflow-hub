package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wfbrokersfetch;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.organization.BrokersRetrieverActivity;
import it.gov.pagopa.payhub.activities.activity.organization.OrganizationBrokeredActiveRetrieverActivity;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.OrganizationPaymentsReportingPagoPaFetchWFClient;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.config.BrokersPaymentsReportingPagoPaFetchWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

@Slf4j
@WorkflowImpl(taskQueues = {PaymentsReportingPagoPaBrokersFetchWFImpl.TASK_QUEUE_BROKERS_PAYMENTS_REPORTING_PAGOPA_FETCH})
public class PaymentsReportingPagoPaBrokersFetchWFImpl implements PaymentsReportingPagoPaBrokersFetchWF, ApplicationContextAware {
  public static final String TASK_QUEUE_BROKERS_PAYMENTS_REPORTING_PAGOPA_FETCH = "PaymentsReportingPagoPaBrokersFetchWF";

  private BrokersRetrieverActivity brokersRetrieverActivity;
  private OrganizationBrokeredActiveRetrieverActivity organizationBrokeredActiveRetrieverActivity;
  private OrganizationPaymentsReportingPagoPaFetchWFClient organizationPaymentsReportingPagoPaFetchWFClient;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    BrokersPaymentsReportingPagoPaFetchWfConfig wfConfig = applicationContext.getBean(BrokersPaymentsReportingPagoPaFetchWfConfig.class);
    brokersRetrieverActivity = wfConfig.buildBrokersRetrieverActivityStub();
    organizationBrokeredActiveRetrieverActivity = wfConfig.buildOrganizationBrokeredActiveRetrieverActivityStub();
    organizationPaymentsReportingPagoPaFetchWFClient = applicationContext.getBean(OrganizationPaymentsReportingPagoPaFetchWFClient.class);
  }

  @Override
  public void retrieve() {
    log.info("Retrieve all Brokered Organization");

    List<Long> brokersId = brokersRetrieverActivity.fetchAllBrokers()
      .stream()
      .map(Broker::getBrokerId)
      .toList();
    log.info("Fetched brokers ID: {}", brokersId);

    for (Long brokerId : brokersId) {
      organizationBrokeredActiveRetrieverActivity.retrieveBrokeredOrganizations(brokerId)
        .stream()
        .map(Organization::getOrganizationId)
        .forEach(organizationPaymentsReportingPagoPaFetchWFClient::retrieveAsyncStart);
    }

    log.info("Retrieving completed");
  }
}

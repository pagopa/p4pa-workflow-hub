package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wfbrokersfetch;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.organization.BrokersRetrieverActivity;
import it.gov.pagopa.payhub.activities.activity.organization.OrganizationBrokeredActiveRetrieverActivity;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflow.config.temporal.TemporalWFImplementationCustomizer;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.OrganizationPaymentsReportingPagoPaFetchWFClient;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.config.BrokersPaymentsReportingPagoPaFetchWfConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_LOW_PRIORITY)
public class PaymentsReportingPagoPaBrokersFetchWFImpl implements PaymentsReportingPagoPaBrokersFetchWF, ApplicationContextAware {

  private BrokersRetrieverActivity brokersRetrieverActivity;
  private OrganizationBrokeredActiveRetrieverActivity organizationBrokeredActiveRetrieverActivity;
  private OrganizationPaymentsReportingPagoPaFetchWFClient organizationPaymentsReportingPagoPaFetchWFClient;

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
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
      .filter(b -> Boolean.TRUE.equals(b.getFlagPaymentsReporting()))
      .map(Broker::getBrokerId)
      .toList();
    log.info("Fetched brokers ID: {}", brokersId);

    for (Long brokerId : brokersId) {
      organizationBrokeredActiveRetrieverActivity.retrieveBrokeredOrganizations(brokerId)
        .stream()
        .filter(o -> Boolean.TRUE.equals(o.getFlagPaymentsReporting()))
        .map(Organization::getOrganizationId)
        .forEach(organizationPaymentsReportingPagoPaFetchWFClient::retrieveAsyncStart);
    }

    log.info("Retrieving completed");
  }
}

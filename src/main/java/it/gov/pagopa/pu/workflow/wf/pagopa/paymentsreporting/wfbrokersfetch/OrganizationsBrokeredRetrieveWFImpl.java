package it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.wfbrokersfetch;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.organization.BrokersRetrieverActivity;
import it.gov.pagopa.payhub.activities.activity.organization.OrganizationBrokeredRetrieverActivity;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.OrganizationPaymentsReportingPagoPaFetchWFClient;
import it.gov.pagopa.pu.workflow.wf.pagopa.paymentsreporting.config.OrganizationsBrokeredRetrieveWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

@Slf4j
@WorkflowImpl(taskQueues = {OrganizationsBrokeredRetrieveWFImpl.TASK_QUEUE_ORGANIZATIONS_BROKERED_RETRIEVE})
public class OrganizationsBrokeredRetrieveWFImpl implements OrganizationsBrokeredRetrieveWF, ApplicationContextAware {
  public static final String TASK_QUEUE_ORGANIZATIONS_BROKERED_RETRIEVE = "OrganizationsBrokeredRetrieveWF";

  private final OrganizationPaymentsReportingPagoPaFetchWFClient organizationPaymentsReportingPagoPaFetchWFClient;

  private BrokersRetrieverActivity brokersRetrieverActivity;
  private OrganizationBrokeredRetrieverActivity organizationBrokeredRetrieverActivity;

  public OrganizationsBrokeredRetrieveWFImpl(OrganizationPaymentsReportingPagoPaFetchWFClient organizationPaymentsReportingPagoPaFetchWFClient) {
    this.organizationPaymentsReportingPagoPaFetchWFClient = organizationPaymentsReportingPagoPaFetchWFClient;
  }

  /**
   * Temporal workflow will not allow to use injection in order to avoid <a href="https://docs.temporal.io/workflows#non-deterministic-change">non-deterministic changes</a> due to dynamic reconfiguration.<BR />
   * Anyway it allows to override ActivityOptions, but actually it's not supporting the override based on the particular workflow.<BR />
   * In {@link it.gov.pagopa.pu.workflow.config.TemporalWFImplementationCustomizer} we are already setting defaults to all workflows.<BR />
   * Use this as an example to override based on the particular workflow.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    OrganizationsBrokeredRetrieveWFConfig wfConfig = applicationContext.getBean(OrganizationsBrokeredRetrieveWFConfig.class);

    brokersRetrieverActivity = wfConfig.buildBrokersRetrieverActivityStub();
    organizationBrokeredRetrieverActivity = wfConfig.buildOrganizationBrokeredRetrieverActivityStub();
  }

  @Override
  public void retrieve() {
    log.info("Retrieve all Brokered Organization");

    List<Long> brokersId = brokersRetrieverActivity.fetchAll()
      .stream()
      .map(Broker::getBrokerId)
      .toList();
    log.info("Fetched brokers ID: {}", brokersId);

    for (Long brokerId : brokersId) {
      List<Long> organizationsId = organizationBrokeredRetrieverActivity.retrieve(brokerId)
        .stream()
        .map(Organization::getOrganizationId)
        .toList();
      log.info("Fetched Organizations for Broked ID: {} - {}", brokersId, organizationsId);
      organizationsId.forEach(organizationPaymentsReportingPagoPaFetchWFClient::retrieve);
    }
    log.info("Retrieving completed");
  }
}

package it.gov.pagopa.pu.workflow.wf.paymentsreporting.broker.wfretrieve;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.organization.BrokersRetrieverActivity;
import it.gov.pagopa.payhub.activities.activity.organization.OrganizationBrokeredRetrieverActivity;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflow.wf.paymentsreporting.broker.activity.ChainOrganizationsBrokered2OrganizationPaymentsReportingActivity;
import it.gov.pagopa.pu.workflow.wf.paymentsreporting.broker.config.OrganizationsBrokeredRetrieveWFConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

@Slf4j
@WorkflowImpl(taskQueues = {OrganizationsBrokeredRetrieveWFImpl.TASK_QUEUE_ORGANIZATIONS_BROKERED_RETRIEVE})
public class OrganizationsBrokeredRetrieveWFImpl implements OrganizationsBrokeredRetrieveWF, ApplicationContextAware {
  public static final String TASK_QUEUE_ORGANIZATIONS_BROKERED_RETRIEVE = "OrganizationsBrokeredRetrieveWF";
  public static final String TASK_QUEUE_ORGANIZATIONS_BROKERED_RETRIEVE_LOCAL_ACTIVITY = "OrganizationsBrokeredRetrieveWF_LOCAL";

  private BrokersRetrieverActivity brokersRetrieverActivity;
  private OrganizationBrokeredRetrieverActivity organizationBrokeredRetrieverActivity;
  private ChainOrganizationsBrokered2OrganizationPaymentsReportingActivity chainOrganizationsBrokered2OrganizationPaymentsReportingActivity;

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
    chainOrganizationsBrokered2OrganizationPaymentsReportingActivity = wfConfig.buildChainBrokeredOrganizations2OrganizationPaymentsReportingActivityStub();
  }

  @Override
  public void retrieve() {
    log.info("Retrieve all Brokered Organization");

    List<Long> brokersId = brokersRetrieverActivity.fetchAll()
      .stream()
      .map(Broker::getBrokerId)
      .toList();
    log.info("Fetched brokers ID: {}", String.join(", ", brokersId.stream().map(String::valueOf).toList()));

    List<Long> organizationsId = brokersId.stream()
      .flatMap(id -> organizationBrokeredRetrieverActivity.retrieve(id).stream())
      .map(Organization::getOrganizationId)
      .toList();
    log.info("Fetched organizations ID: {}", String.join(", ", organizationsId.stream().map(String::valueOf).toList()));

    organizationsId.forEach(chainOrganizationsBrokered2OrganizationPaymentsReportingActivity::chain);
    log.info("Retrieving completed");
  }
}

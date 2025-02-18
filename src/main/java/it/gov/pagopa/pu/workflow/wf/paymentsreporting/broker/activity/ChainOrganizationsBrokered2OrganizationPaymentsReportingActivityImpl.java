package it.gov.pagopa.pu.workflow.wf.paymentsreporting.broker.activity;

import io.temporal.spring.boot.ActivityImpl;
import it.gov.pagopa.pu.workflow.wf.paymentsreporting.broker.wfretrieve.OrganizationsBrokeredRetrieveWFImpl;
import it.gov.pagopa.pu.workflow.wf.paymentsreporting.pagopa.OrganizationPaymentsReportingPagoPaFetchWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ActivityImpl(taskQueues = OrganizationsBrokeredRetrieveWFImpl.TASK_QUEUE_ORGANIZATIONS_BROKERED_RETRIEVE_LOCAL_ACTIVITY)
public class ChainOrganizationsBrokered2OrganizationPaymentsReportingActivityImpl implements ChainOrganizationsBrokered2OrganizationPaymentsReportingActivity {

  private final OrganizationPaymentsReportingPagoPaFetchWFClient organizationPaymentsReportingPagoPaFetchWFClient;

  public ChainOrganizationsBrokered2OrganizationPaymentsReportingActivityImpl(OrganizationPaymentsReportingPagoPaFetchWFClient organizationPaymentsReportingPagoPaFetchWFClient) {
    this.organizationPaymentsReportingPagoPaFetchWFClient = organizationPaymentsReportingPagoPaFetchWFClient;
  }

  @Override
  public void chain(Long organizationId) {
    log.info("Chaining WF to OrganizationPaymentsReportingPagoPaFetchWF after fetching id {}", organizationId);
    organizationPaymentsReportingPagoPaFetchWFClient.retrieve(organizationId);
  }
}

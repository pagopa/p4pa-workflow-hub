package it.gov.pagopa.pu.workflow.wf.paymentsreporting.broker.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ChainOrganizationsBrokered2OrganizationPaymentsReportingActivity {

  @ActivityMethod
  void chain(Long organizationId);
}

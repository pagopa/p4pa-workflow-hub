package it.gov.pagopa.pu.workflow.wf.pagopa.send.wfsendnotification;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.sendnotification.dto.generated.LegalFactCategoryDTO;

import java.io.IOException;

@WorkflowInterface
public interface SendLegalFactProcessWF {

  @WorkflowMethod
  void fetchSendLegalFact(String sendNotificationId, String legalFactId, LegalFactCategoryDTO category);
}

package it.gov.pagopa.pu.workflow.wf.debtposition.massive.wfmassiveibanupdatetosync;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MassiveIbanUpdateWFToSync {
  @WorkflowMethod
  void massiveIbanUpdate(Long orgId, Long dptoId, String oldIban, String newIban, String oldPostalIban, String newPostalIban);
}

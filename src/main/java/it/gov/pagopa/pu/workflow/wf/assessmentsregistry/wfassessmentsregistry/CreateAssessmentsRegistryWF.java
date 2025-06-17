package it.gov.pagopa.pu.workflow.wf.assessmentsregistry.wfassessmentsregistry;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import java.util.List;

@WorkflowInterface
public interface CreateAssessmentsRegistryWF {

  @WorkflowMethod
  void createAssessmentsRegistry(DebtPositionDTO debtPositionDTO, List<String> iudList);
}

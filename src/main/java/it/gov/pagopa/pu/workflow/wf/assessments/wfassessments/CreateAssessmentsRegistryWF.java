package it.gov.pagopa.pu.workflow.wf.assessments.wfassessments;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import java.util.List;

/**
 * Workflow interface for the Assessment creation
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1816559617/Creazione+Registro+Accertamenti>Confluence page</a>
 * */
@WorkflowInterface
public interface CreateAssessmentsRegistryWF {

  @WorkflowMethod
  void createAssessmentsRegistry(DebtPositionDTO debtPositionDTO, List<String> iudList);
}

package it.gov.pagopa.pu.workflow.utilities;

import it.gov.pagopa.pu.workflow.exception.custom.WorkflowInternalErrorException;
import org.springframework.stereotype.Component;

@Component
public class Utilities {

  private Utilities(){}

  public static String generateWorkflowId(Long id, String workflow){
    if (id == null || workflow == null) {
      throw new WorkflowInternalErrorException("The ID or the workflow must not be null");
    }
    return String.format("%s-%d", workflow, id);
  }
}

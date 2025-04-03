package it.gov.pagopa.pu.workflow.service.exportfile;

import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;

public interface ExportFileService {
  WorkflowCreatedDTO exportFileExpirationHandler(Long exportFileId);
}

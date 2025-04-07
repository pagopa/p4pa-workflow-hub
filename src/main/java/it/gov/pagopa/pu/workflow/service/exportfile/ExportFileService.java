package it.gov.pagopa.pu.workflow.service.exportfile;

import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;

public interface ExportFileService {
  WorkflowCreatedDTO expireExportFile(Long exportFileId);
  WorkflowCreatedDTO create(Long exportFileId, ExportFile.ExportFileTypeEnum exportFileType);
}

package it.gov.pagopa.pu.workflow.service.wf.exportfile;

import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;

public interface ExportFileService {
  WorkflowCreatedDTO expireExportFile(Long exportFileId);
  WorkflowCreatedDTO exportFile(Long exportFileId, ExportFile.ExportFileTypeEnum exportFileType);
}

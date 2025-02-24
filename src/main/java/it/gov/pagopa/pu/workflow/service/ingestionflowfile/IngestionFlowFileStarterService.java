package it.gov.pagopa.pu.workflow.service.ingestionflowfile;

import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;

public interface IngestionFlowFileStarterService {
  String ingest(long ingestionFlowFileId, IngestionFlowFile.FlowFileTypeEnum flowFileType);
}

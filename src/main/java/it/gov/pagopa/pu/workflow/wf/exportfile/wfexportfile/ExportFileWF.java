package it.gov.pagopa.pu.workflow.wf.exportfile.wfexportfile;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile;

/**
 * Workflow interface for file export
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1470169164/Flussi+di+caricamento+e+download+dei+file#3.1.1.-Richiesta-di-export>Confluence page</a>
 * */

@WorkflowInterface
public interface ExportFileWF {

    @WorkflowMethod
    void exportFile(Long exportFileId, ExportFile.ExportFileTypeEnum exportFileType);
}

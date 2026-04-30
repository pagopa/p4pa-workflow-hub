package it.gov.pagopa.pu.workflow.wf.exportfile.export.wfexportfile;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile;

/**
 * Workflow interface for file export
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1642758254/Export+File>Confluence page</a>
 * */

@WorkflowInterface
public interface ExportFileWF {

    @WorkflowMethod
    void exportFile(Long exportFileId, ExportFile.ExportFileTypeEnum exportFileType);
}

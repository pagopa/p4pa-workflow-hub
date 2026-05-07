package it.gov.pagopa.pu.workflow.wf.email.wf;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import it.gov.pagopa.payhub.activities.dto.email.EmailDTO;

/**
 * Workflow to test email send
 * @see <a href=https://pagopa.atlassian.net/wiki/spaces/SPAC/pages/1642758254/Export+File>Confluence page</a>
 * */
@WorkflowInterface
public interface SendGenericEmailWF {

    @WorkflowMethod
    void sendGenericEmail(EmailDTO emailDTO, Long brokerId);
}

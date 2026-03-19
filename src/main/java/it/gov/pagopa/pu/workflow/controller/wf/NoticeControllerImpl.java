package it.gov.pagopa.pu.workflow.controller.wf;

import it.gov.pagopa.pu.workflow.controller.generated.NoticeApi;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.DeleteMassiveNoticesFileWFClient;
import it.gov.pagopa.pu.workflow.wf.ingestionflow.notice.MassiveNoticesGenerationWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class NoticeControllerImpl implements NoticeApi {
  private final MassiveNoticesGenerationWFClient massiveNoticesGenerationWFClient;
  private final DeleteMassiveNoticesFileWFClient deleteMassiveNoticesFileWFClient;

  public NoticeControllerImpl(MassiveNoticesGenerationWFClient massiveNoticesGenerationWFClient, DeleteMassiveNoticesFileWFClient deleteMassiveNoticesFileWFClient) {
    this.massiveNoticesGenerationWFClient = massiveNoticesGenerationWFClient;
    this.deleteMassiveNoticesFileWFClient = deleteMassiveNoticesFileWFClient;
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> generateMassive(Long ingestionFlowFileId) {
    log.info("Starting massive notice generation workflow for ingestionFlowFileId: {}", ingestionFlowFileId);
    WorkflowCreatedDTO wfCreated = massiveNoticesGenerationWFClient.generate(ingestionFlowFileId);
    return ResponseEntity.ok(wfCreated);
  }

  @Override
  public ResponseEntity<Void> deleteMassive(Long ingestionFlowFileId) {
    log.info("Deleting massive notices file workflow for ingestionFlowFileId: {}", ingestionFlowFileId);
    deleteMassiveNoticesFileWFClient.delete(ingestionFlowFileId);
    return ResponseEntity.ok().build();
  }
}

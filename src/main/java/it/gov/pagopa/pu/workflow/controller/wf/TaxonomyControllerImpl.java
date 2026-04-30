package it.gov.pagopa.pu.workflow.controller.wf;

import it.gov.pagopa.pu.workflow.controller.generated.TaxonomyApi;
import it.gov.pagopa.pu.workflow.dto.generated.WorkflowCreatedDTO;
import it.gov.pagopa.pu.workflow.wf.pagopa.taxonomy.TaxonomyWFClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TaxonomyControllerImpl implements TaxonomyApi {

  private final TaxonomyWFClient taxonomyWFClient;

  public TaxonomyControllerImpl(TaxonomyWFClient taxonomyWFClient) {
    this.taxonomyWFClient = taxonomyWFClient;
  }

  @Override
  public ResponseEntity<WorkflowCreatedDTO> synchronizeTaxonomy() {
    log.info("Requesting to synchronize taxonomy with PagoPa");
    WorkflowCreatedDTO wfCreated = taxonomyWFClient.synchronizeTaxonomy();
    return ResponseEntity.ok(wfCreated);
  }
}

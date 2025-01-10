package it.gov.pagopa.pu.workflow.controller.debtposition;

import it.gov.pagopa.pu.workflow.controller.generated.DebtPositionApi;
import it.gov.pagopa.pu.workflow.dto.generated.CreateDpSyncResponseDTO;
import it.gov.pagopa.pu.workflow.dto.generated.DebtPositionRequestDTO;
import it.gov.pagopa.pu.workflow.service.debtposition.DebtPositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class DebtPositionControllerImpl implements DebtPositionApi {

  private final DebtPositionService service;

  public DebtPositionControllerImpl(DebtPositionService service) {
    this.service = service;
  }

  @Override
  public ResponseEntity<CreateDpSyncResponseDTO> createDpSync(DebtPositionRequestDTO debtPositionRequestDTO) {
    CreateDpSyncResponseDTO createDpSyncResponseDTO = service.createDPSync(debtPositionRequestDTO);
    return new ResponseEntity<>(createDpSyncResponseDTO, HttpStatus.OK);
  }
}

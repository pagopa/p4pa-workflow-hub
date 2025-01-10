package it.gov.pagopa.pu.workflow.service.debtposition;

import it.gov.pagopa.payhub.activities.dto.debtposition.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.dto.generated.CreateDpSyncResponseDTO;
import it.gov.pagopa.pu.workflow.dto.generated.DebtPositionRequestDTO;
import it.gov.pagopa.pu.workflow.wf.debtposition.createdp.CreateDebtPositionWfClient;
import it.gov.pagopa.pu.workflow.wf.debtposition.mapper.DebtPositionMapper;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionServiceImpl implements DebtPositionService {

  private final CreateDebtPositionWfClient client;
  private final DebtPositionMapper debtPositionMapper;


  public DebtPositionServiceImpl(CreateDebtPositionWfClient client, DebtPositionMapper debtPositionMapper) {
    this.client = client;
    this.debtPositionMapper = debtPositionMapper;
  }

  @Override
  public CreateDpSyncResponseDTO createDPSync(DebtPositionRequestDTO debtPositionRequestDTO) {
    DebtPositionDTO debtPosition = debtPositionMapper.map(debtPositionRequestDTO);
    String workflowId = client.createDPSync(debtPosition);

    return CreateDpSyncResponseDTO.builder()
      .workflowId(workflowId)
      .build();
  }
}

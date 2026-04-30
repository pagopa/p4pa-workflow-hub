package it.gov.pagopa.pu.workflow.controller;

import it.gov.pagopa.pu.workflow.controller.generated.ScheduleApi;
import it.gov.pagopa.pu.workflow.dto.generated.ScheduleInfoDTO;
import it.gov.pagopa.pu.workflow.enums.ScheduleEnum;
import it.gov.pagopa.pu.workflow.service.temporal.WorkflowScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ScheduleControllerImpl implements ScheduleApi {

  private final WorkflowScheduleService service;

  public ScheduleControllerImpl(WorkflowScheduleService service) {
    this.service = service;
  }

  @Override
  public ResponseEntity<ScheduleInfoDTO> getScheduleInfo(ScheduleEnum scheduleId) {
    log.info("Requesting schedule info of {}", scheduleId);
    return ResponseEntity.ok(service.getScheduleInfo(scheduleId));
  }
}

package it.gov.pagopa.pu.workflow.wf.assessmentsregistry.wfassessmentsregistry;

import io.temporal.spring.boot.WorkflowImpl;
import it.gov.pagopa.payhub.activities.activity.assessments.AssessmentsRegistryCreationActivity;
import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflow.utilities.TaskQueueConstants;
import it.gov.pagopa.pu.workflow.wf.assessmentsregistry.config.CreateAssessmentsRegistryWFConfig;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
@WorkflowImpl(taskQueues = TaskQueueConstants.TASK_QUEUE_ASSESSMENTS_REGISTRY_RESERVED_CREATION)
public class CreateAssessmentsRegistryWFImpl implements CreateAssessmentsRegistryWF, ApplicationContextAware {

  private AssessmentsRegistryCreationActivity assessmentsRegistryCreationActivity;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    CreateAssessmentsRegistryWFConfig wfConfig = applicationContext.getBean(CreateAssessmentsRegistryWFConfig.class);
    assessmentsRegistryCreationActivity = wfConfig.buildAssessmentsRegistryCreationActivityStub();
  }

  @Override
  public void createAssessmentsRegistry(DebtPositionDTO debtPositionDTO,
    List<String> iudList) {
    log.info("Creating assessments registry by debtPositionDTO and iuds");
    assessmentsRegistryCreationActivity
      .createAssessmentsRegistryByDebtPositionDTOAndIudList(debtPositionDTO, iudList);
    log.debug("Assessments registry creation is complete with debtPositionDTO {} and iudList {}", debtPositionDTO, iudList);
  }
}

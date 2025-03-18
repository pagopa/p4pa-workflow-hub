package it.gov.pagopa.pu.workflow.repository;

import it.gov.pagopa.pu.workflow.model.WorkflowType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "workflow-types")
public interface WorkflowTypeRepository extends JpaRepository<WorkflowType, Long> {
}

package it.gov.pagopa.pu.workflow.repository;

import it.gov.pagopa.pu.workflow.model.DebtPositionWorkflowType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "debt-position-workflow-types")
public interface DebtPositionWorkflowTypeRepository extends JpaRepository<DebtPositionWorkflowType, Long> {
}

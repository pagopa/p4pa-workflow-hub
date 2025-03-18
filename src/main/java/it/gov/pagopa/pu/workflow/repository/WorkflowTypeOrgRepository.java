package it.gov.pagopa.pu.workflow.repository;

import it.gov.pagopa.pu.workflow.model.WorkflowTypeOrg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "workflow-type-orgs")
public interface WorkflowTypeOrgRepository extends JpaRepository<WorkflowTypeOrg, Long> {
}

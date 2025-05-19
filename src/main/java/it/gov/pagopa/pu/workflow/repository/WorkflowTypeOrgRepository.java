package it.gov.pagopa.pu.workflow.repository;

import it.gov.pagopa.pu.workflow.model.WorkflowTypeOrg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.annotation.Nonnull;

@RepositoryRestResource(path = "workflow-type-orgs")
public interface WorkflowTypeOrgRepository extends JpaRepository<WorkflowTypeOrg, Long> {

  @RestResource(exported = false)
  @Nonnull
  @Override
  <S extends WorkflowTypeOrg> S save(@Nonnull S entity);
}

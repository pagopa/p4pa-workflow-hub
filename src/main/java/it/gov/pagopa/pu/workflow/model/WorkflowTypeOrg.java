package it.gov.pagopa.pu.workflow.model;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.WfExecutionConfig;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;

@Entity
@Table(name = "workflow_type_org")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = false)
public class WorkflowTypeOrg extends BaseEntity implements Serializable {

  @Id
  @NotNull
  private Long debtPositionTypeOrgId;
  @NotNull
  private Long workflowTypeId;
  @NotNull
  @JdbcTypeCode(SqlTypes.JSON)
  private WfExecutionConfig defaultExecutionConfig;
}

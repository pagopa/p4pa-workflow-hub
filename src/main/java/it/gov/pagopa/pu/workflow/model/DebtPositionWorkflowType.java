package it.gov.pagopa.pu.workflow.model;

import it.gov.pagopa.payhub.activities.dto.debtposition.WfExecutionConfig;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;

@Entity
@Table(name = "debt_position_workflow_type")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class DebtPositionWorkflowType extends BaseEntity implements Serializable {

  @Id
  @NotNull
  private Long debtPositionId;
  private Long workflowTypeOrgId;
  @JdbcTypeCode(SqlTypes.JSON)
  private WfExecutionConfig executionConfig;
}

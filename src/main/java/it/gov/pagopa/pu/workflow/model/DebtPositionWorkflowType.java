package it.gov.pagopa.pu.workflow.model;

import it.gov.pagopa.pu.workflow.model.executionconfig.WfExecutionConfig;
import jakarta.persistence.*;
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
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "debt_position_workflow_type_generator")
  @SequenceGenerator(name = "debt_position_workflow_type_generator", sequenceName = "debt_position_workflow_type_seq", allocationSize = 1)
  private Long debtPositionWorkflowTypeId;
  @NotNull
  private Long debtPositionId;
  private Long workflowTypeOrgId;
  @NotNull
  private Long workflowId;
  @JdbcTypeCode(SqlTypes.JSON)
  private WfExecutionConfig executionConfig;
}

package it.gov.pagopa.pu.workflow.model;

import it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.WfExecutionConfig;
import jakarta.persistence.*;
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
@Table(name = "workflow_type")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = false)
public class WorkflowType extends BaseEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workflow_type_generator")
  @SequenceGenerator(name = "workflow_type_generator", sequenceName = "workflow_type_seq", allocationSize = 1)
  private Long workflowTypeId;
  @NotNull
  private String description;
  @NotNull
  @JdbcTypeCode(SqlTypes.JSON)
  private WfExecutionConfig defaultExecutionConfig;
}

package it.gov.pagopa.pu.workflow.model;

import it.gov.pagopa.payhub.activities.dto.debtposition.WfExecutionConfig;
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
@Table(name = "workflow_type_org")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class WorkflowTypeOrg extends BaseEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workflow_type_org_generator")
  @SequenceGenerator(name = "workflow_type_org_generator", sequenceName = "workflow_type_org_seq", allocationSize = 1)
  private Long workflowTypeOrgId;
  @NotNull
  private Long workflowTypeId;
  @NotNull
  private Long debtPositionTypeOrgId;
  @JdbcTypeCode(SqlTypes.JSON)
  private WfExecutionConfig defaultExecutionConfig;
}

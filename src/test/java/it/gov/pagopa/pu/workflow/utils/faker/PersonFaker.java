package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.pu.debtposition.dto.generated.PersonDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PersonRequestDTO;

public class PersonFaker {

  public static PersonDTO buildPersonDTO() {
    return PersonDTO.builder()
      .entityType(PersonDTO.EntityTypeEnum.F)
      .fiscalCode("fiscalCode")
      .fullName("fullName")
      .address("address")
      .civic("civic")
      .postalCode("postalCode")
      .location("location")
      .province("province")
      .nation("nation")
      .email("email@test.it")
      .build();
  }

  public static PersonRequestDTO buildPersonRequestDTO() {
    return PersonRequestDTO.builder()
      .entityType(PersonRequestDTO.EntityTypeEnum.F)
      .fiscalCode("fiscalCode")
      .fullName("fullName")
      .address("address")
      .civic("civic")
      .postalCode("postalCode")
      .location("location")
      .province("province")
      .nation("nation")
      .email("email@test.it")
      .build();
  }
}

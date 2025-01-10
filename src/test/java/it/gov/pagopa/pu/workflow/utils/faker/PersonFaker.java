package it.gov.pagopa.pu.workflow.utils.faker;

import it.gov.pagopa.payhub.activities.dto.PersonDTO;
import it.gov.pagopa.pu.workflow.dto.generated.PersonRequestDTO;

public class PersonFaker {

    public static PersonDTO buildPersonDTO(){
        return PersonDTO.builder()
                .uniqueIdentifierType("uniqueIdentifierType")
                .uniqueIdentifierCode("uniqueIdentifierCode")
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

  public static PersonRequestDTO buildPersonRequestDTO(){
    return PersonRequestDTO.builder()
      .uniqueIdentifierType("uniqueIdentifierType")
      .uniqueIdentifierCode("uniqueIdentifierCode")
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

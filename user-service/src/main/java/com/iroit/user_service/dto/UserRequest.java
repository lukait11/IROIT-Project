package com.iroit.user_service.dto;

import java.time.LocalDate;

// Request body shape for create/update - kept separate from the User entity
// so incoming JSON can't set persistence-only fields (e.g. userId) via mass assignment.
public class UserRequest {

  private String firstName;
  private String lastName;
  private LocalDate dateOfBirth;

  public String getFirstName() {
    return firstName;
  }
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }
  public String getLastName() {
    return lastName;
  }
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }
  public LocalDate getDateOfBirth() {
    return dateOfBirth;
  }
  public void setDateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

}

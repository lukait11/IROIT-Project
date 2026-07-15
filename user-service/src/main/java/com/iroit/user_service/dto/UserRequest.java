package com.iroit.user_service.dto;

import java.time.LocalDate;

// Request body shape for create/update - kept separate from the User entity
// so incoming JSON can't set persistence-only fields (e.g. userId) via mass assignment.
public record UserRequest(String firstName, String lastName, LocalDate dateOfBirth) {
}

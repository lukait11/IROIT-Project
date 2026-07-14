package com.iroit.user_service.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.sql.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.iroit.user_service.exceptions.InvalidRequestException;
import com.iroit.user_service.exceptions.ResourceNotFoundException;
import com.iroit.user_service.models.User;
import com.iroit.user_service.services.UserService;

@WebMvcTest(UserController.class)
class UserControllerTest {

  @Autowired
  private MockMvcTester mvc;

  @MockitoBean
  private UserService userService;

  @Test
  void getUsers_returnsOkWithBody() {
    when(userService.getUsers()).thenReturn(java.util.List.of(
        new User("Ada", "Lovelace", Date.valueOf("1815-12-10"))));

    mvc.get().uri("/")
        .assertThat()
        .hasStatus(HttpStatus.OK)
        .bodyText().contains("Ada");
  }

  @Test
  void getUser_found_returnsOk() {
    when(userService.getUserById(1L))
        .thenReturn(new User("Ada", "Lovelace", Date.valueOf("1815-12-10")));

    mvc.get().uri("/1")
        .assertThat()
        .hasStatus(HttpStatus.OK)
        .bodyText().contains("Ada");
  }

  @Test
  void getUser_notFound_returns404() {
    when(userService.getUserById(1L)).thenThrow(new ResourceNotFoundException("The user was not found!"));

    mvc.get().uri("/1")
        .assertThat()
        .hasStatus(HttpStatus.NOT_FOUND);
  }

  @Test
  void createUser_returnsOk() {
    User user = new User("Ada", "Lovelace", Date.valueOf("1815-12-10"));
    when(userService.createUser(any())).thenReturn(user);

    mvc.post().uri("/")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\",\"dateOfBirth\":\"1815-12-10\"}")
        .assertThat()
        .hasStatus(HttpStatus.OK);
  }

  @Test
  void createUser_invalidData_returns400() {
    when(userService.createUser(any())).thenThrow(new InvalidRequestException("Invalid user data supplied!"));

    mvc.post().uri("/")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}")
        .assertThat()
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  void updateUser_returnsOk() {
    User user = new User("Augusta", "King", Date.valueOf("1815-12-10"));
    when(userService.updateUser(anyLong(), any())).thenReturn(user);

    mvc.put().uri("/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"firstName\":\"Augusta\",\"lastName\":\"King\",\"dateOfBirth\":\"1815-12-10\"}")
        .assertThat()
        .hasStatus(HttpStatus.OK)
        .bodyText().contains("Augusta");
  }

  @Test
  void updateUser_notFound_returns404() {
    when(userService.updateUser(anyLong(), any())).thenThrow(new ResourceNotFoundException("The user was not found!"));

    mvc.put().uri("/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"firstName\":\"Augusta\",\"lastName\":\"King\",\"dateOfBirth\":\"1815-12-10\"}")
        .assertThat()
        .hasStatus(HttpStatus.NOT_FOUND);
  }

  @Test
  void deleteUser_returnsOk() {
    mvc.delete().uri("/1")
        .assertThat()
        .hasStatus(HttpStatus.OK);
  }

  @Test
  void deleteUser_notFound_returns404() {
    org.mockito.Mockito.doThrow(new ResourceNotFoundException("The user was not found!"))
        .when(userService).deleteUser(1L);

    mvc.delete().uri("/1")
        .assertThat()
        .hasStatus(HttpStatus.NOT_FOUND);
  }

}

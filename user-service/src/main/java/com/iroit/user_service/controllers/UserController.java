package com.iroit.user_service.controllers;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.iroit.user_service.models.User;
import com.iroit.user_service.services.UserService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
public class UserController {
  
  @Autowired
  private UserService userService;

  @GetMapping
  public ResponseEntity<List<User>> getUsers() {
    try {
      List<User> result = userService.getUsers();
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Collections.emptyList());
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<User> getUsers(@PathVariable Long userId) {
    try {
      User result = userService.getUserById(userId);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
  }
  
  @PostMapping()
  public ResponseEntity<User> createUser(@RequestBody User user) {
    try {
      User result = userService.createUser(user);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      String cause = e.getCause().getMessage();
      switch (cause) {

        case "Data":
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        default:
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
          
      }
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<User> updateUser(@PathVariable Long userId, @RequestBody User user) {
    try {
      User result = userService.updateUser(userId, user);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      String cause = e.getCause().getMessage();
      switch (cause) {

        case "ID":
        case "Data":
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        default:
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);

      }
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
    try {
      userService.deleteUser(userId);
      return ResponseEntity.ok("Succesfully deleted");
    } catch (Exception e) {
      String cause = e.getCause().getMessage();
      switch (cause) {

        case "ID":
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        default:
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);

      }
    }
  }
  

}

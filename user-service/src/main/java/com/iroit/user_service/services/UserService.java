package com.iroit.user_service.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.iroit.user_service.exceptions.InvalidRequestException;
import com.iroit.user_service.exceptions.ResourceNotFoundException;
import com.iroit.user_service.models.User;
import com.iroit.user_service.repositories.UserRepository;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

//#region Get methods
  public List<User> getUsers() {
    return userRepository.findAll();
  }

  public User getUserById(Long userId) {
    if (userId == null || userId < 0)
      throw new InvalidRequestException("Invalid ID supplied!");

    return userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("The user was not found!"));
  }
//#endregion

//#region Post methods
  public User createUser(User user) {
    if (user == null)
      throw new InvalidRequestException("Invalid user data supplied!");

    return userRepository.save(user);
  }
//#endregion

//#region Put methods
  public User updateUser(Long userId, User user) {
    if (userId == null || userId < 0)
      throw new InvalidRequestException("Invalid ID supplied!");

    if (user == null)
      throw new InvalidRequestException("Invalid user data supplied!");

    User foundUser = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("The user was not found!"));

    foundUser.setFirstName(user.getFirstName());
    foundUser.setLastName(user.getLastName());
    foundUser.setDateOfBirth(user.getDateOfBirth());

    return userRepository.save(foundUser);
  }
//#endregion

//#region Delete methods
  public void deleteUser(Long userId) {
    if (userId == null || userId < 0)
      throw new InvalidRequestException("Invalid ID supplied!");

    if (!userRepository.existsById(userId))
      throw new ResourceNotFoundException("The user was not found!");

    userRepository.deleteById(userId);
  }
//#endregion

}

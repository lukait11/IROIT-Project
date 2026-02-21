package com.iroit.user_service.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iroit.user_service.models.User;
import com.iroit.user_service.repositories.UserRepository;

@Service
public class UserService {
  
  @Autowired
  private UserRepository userRepository;

//#region Get methods
  public List<User> getUsers() throws Exception {
    List<User> result = userRepository.findAll();

    if (result == null || result.size() == 0)
      throw new Exception("No users found!");

    return result;
  }

  public User getUserById(Long userId) throws Exception {
    if (userId == null || userId < 0)
      throw new Exception("Invalid ID supplied!");

    Optional<User> result = userRepository.findById(userId);

    if (result.isEmpty())
      throw new Exception("The user was not found!");

    return result.get();
  }
//#endregion

//#region Post methods
  public User createUser(User user) throws Exception {

    if (user == null)
      throw new Exception("Invalid user data supplied", new Throwable("Data"));

    User result = userRepository.save(user);

    if (result == null)
      throw new Exception("Couldn't create the user!");

    return result;
  }
//#endregion

//#region Put methods
  public User updateUser(Long userId, User user) throws Exception {
    if (userId == null || userId < 0)
      throw new Exception("Invalid ID supplied!", new Throwable("ID"));

    if (user == null)
      throw new Exception("Invalid user data supplied", new Throwable("Data"));

    User result = userRepository.findById(userId).map(foundUser -> {
      foundUser.setFirstName(user.getFirstName());
      foundUser.setLastName(user.getLastName());
      foundUser.setDateOfBirth(user.getDateOfBirth());
      return userRepository.save(user);
    }).orElseThrow(() -> new Exception("Couldn't update the user!"));

    return result;
  }
//#endregion

//#region Delete methods
  public void deleteUser(Long userId) throws Exception {
    if (userId == null || userId < 0)
      throw new Exception("Invalid ID supplied!", new Throwable("ID"));

    try {
      userRepository.deleteById(userId);
    } catch (Exception e) {
      throw new Exception("Couldn't delete the user!");
      // TODO add logging
    }
  }
//#endregion

}

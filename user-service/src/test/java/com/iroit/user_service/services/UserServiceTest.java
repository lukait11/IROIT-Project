package com.iroit.user_service.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iroit.user_service.exceptions.InvalidRequestException;
import com.iroit.user_service.exceptions.ResourceNotFoundException;
import com.iroit.user_service.models.User;
import com.iroit.user_service.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  @Test
  void getUsers_returnsWhateverTheRepositoryHas() {
    User user = new User("Ada", "Lovelace", LocalDate.of(1815, Month.DECEMBER, 10));
    when(userRepository.findAll()).thenReturn(List.of(user));

    List<User> result = userService.getUsers();

    assertThat(result).containsExactly(user);
  }

  @Test
  void getUsers_emptyRepository_returnsEmptyList() {
    when(userRepository.findAll()).thenReturn(List.of());

    List<User> result = userService.getUsers();

    assertThat(result).isEmpty();
  }

  @Test
  void getUserById_negativeId_throwsInvalidRequestException() {
    assertThatThrownBy(() -> userService.getUserById(-1L))
        .isInstanceOf(InvalidRequestException.class);
  }

  @Test
  void getUserById_nullId_throwsInvalidRequestException() {
    assertThatThrownBy(() -> userService.getUserById(null))
        .isInstanceOf(InvalidRequestException.class);
  }

  @Test
  void getUserById_notFound_throwsResourceNotFoundException() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.getUserById(1L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void getUserById_found_returnsUser() {
    User user = new User("Ada", "Lovelace", LocalDate.of(1815, Month.DECEMBER, 10));
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    User result = userService.getUserById(1L);

    assertThat(result).isEqualTo(user);
  }

  @Test
  void createUser_nullUser_throwsInvalidRequestException() {
    assertThatThrownBy(() -> userService.createUser(null))
        .isInstanceOf(InvalidRequestException.class);

    verify(userRepository, never()).save(any());
  }

  @Test
  void createUser_validUser_savesAndReturnsIt() {
    User user = new User("Ada", "Lovelace", LocalDate.of(1815, Month.DECEMBER, 10));
    when(userRepository.save(user)).thenReturn(user);

    User result = userService.createUser(user);

    assertThat(result).isEqualTo(user);
    verify(userRepository).save(user);
  }

  @Test
  void updateUser_invalidId_throwsInvalidRequestException() {
    User user = new User("Ada", "Lovelace", LocalDate.of(1815, Month.DECEMBER, 10));

    assertThatThrownBy(() -> userService.updateUser(-1L, user))
        .isInstanceOf(InvalidRequestException.class);

    verify(userRepository, never()).save(any());
  }

  @Test
  void updateUser_nullBody_throwsInvalidRequestException() {
    assertThatThrownBy(() -> userService.updateUser(1L, null))
        .isInstanceOf(InvalidRequestException.class);

    verify(userRepository, never()).save(any());
  }

  @Test
  void updateUser_notFound_throwsResourceNotFoundException() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());
    User user = new User("Ada", "Lovelace", LocalDate.of(1815, Month.DECEMBER, 10));

    assertThatThrownBy(() -> userService.updateUser(1L, user))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(userRepository, never()).save(any());
  }

  @Test
  void updateUser_existingUser_savesTheFoundEntityWithNewFieldsNotTheIncomingOne() {
    User existing = new User("Ada", "Lovelace", LocalDate.of(1815, Month.DECEMBER, 10));
    when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    User incoming = new User("Augusta", "King", LocalDate.of(1815, Month.DECEMBER, 10));
    User result = userService.updateUser(1L, incoming);

    ArgumentCaptor<User> savedCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository, times(1)).save(savedCaptor.capture());

    // Regression guard: the saved entity must be the one fetched via findById
    // (so it keeps the existing row's ID), not the detached incoming request body.
    assertThat(savedCaptor.getValue()).isSameAs(existing);
    assertThat(result.getFirstName()).isEqualTo("Augusta");
    assertThat(result.getLastName()).isEqualTo("King");
  }

  @Test
  void deleteUser_invalidId_throwsInvalidRequestException() {
    assertThatThrownBy(() -> userService.deleteUser(-1L))
        .isInstanceOf(InvalidRequestException.class);

    verify(userRepository, never()).deleteById(any());
  }

  @Test
  void deleteUser_notFound_throwsResourceNotFoundException() {
    when(userRepository.existsById(1L)).thenReturn(false);

    assertThatThrownBy(() -> userService.deleteUser(1L))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(userRepository, never()).deleteById(any());
  }

  @Test
  void deleteUser_existing_deletesIt() {
    when(userRepository.existsById(1L)).thenReturn(true);

    userService.deleteUser(1L);

    verify(userRepository).deleteById(1L);
  }

}

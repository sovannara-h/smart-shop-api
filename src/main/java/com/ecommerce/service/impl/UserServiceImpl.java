package com.ecommerce.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.exception.UserAlreadyExistsException;
import com.ecommerce.exception.UserException;
import com.ecommerce.exception.UserNotFoundException;
import com.ecommerce.model.dto.SignUpRequest;
import com.ecommerce.model.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.interfaces.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

private boolean existsByEmail(String email) {
    if (email == null) {
      return false;
    }
    return userRepository.existsByEmail(email);
  }
  
  @Override
  @Transactional(readOnly=true)
  public User findUserById(Long id) throws UserNotFoundException {
    if (id == null) {
      throw new IllegalArgumentException("L'ID utilisateur ne peut pas être nul");
    }
    return userRepository
        .findById(id)
        .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
  }

  @Override
  @Transactional(readOnly=true)
  public User findUserByEmail(String email) throws UserNotFoundException {
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("L'email ne peut pas être nul ou vide");
    }
    return userRepository
        .findByEmail(email)
        .orElseThrow(
            () -> new UserNotFoundException("Utilisateur non trouvé avec l'email: " + email));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public User updateUser(Long id, User user) throws UserNotFoundException {
    if (id == null || user == null) {
      throw new IllegalArgumentException(
          "L'ID et les données utilisateur ne peuvent pas être nuls");
    }

    User existingUser = findUserById(id);

    if (user.getEmail() != null) {
      // Vérifier si le nouvel email n'est pas déjà utilisé par un autre utilisateur
      if (!user.getEmail().equals(existingUser.getEmail()) && existsByEmail(user.getEmail())) {
        throw new UserAlreadyExistsException("Cet email est déjà utilisé");
      }
      existingUser.setEmail(user.getEmail());
    }

    if (user.getPassword() != null) {
      existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    log.info("Mise à jour de l'utilisateur: {}", id);
    return userRepository.save(existingUser);
  }

  @Override
  @Transactional
  public User registerUser(SignUpRequest signUpRequest) {
    if (signUpRequest == null) {
      throw new IllegalArgumentException("La demande d'inscription ne peut pas être nulle");
    }
    if (signUpRequest.getEmail() == null || signUpRequest.getPassword() == null) {
      throw new IllegalArgumentException("L'email et le mot de passe sont obligatoires");
    }

    if (existsByEmail(signUpRequest.getEmail())) {
      throw new UserAlreadyExistsException("Un utilisateur avec cet email existe déjà");
    }

    User user = new User();
    user.setEmail(signUpRequest.getEmail());
    user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
    return userRepository.save(user);
  }

  @Override
  @Transactional
  public void deleteUser(Long id) throws UserNotFoundException {
    if (!userRepository.existsById(id)) {
      throw new UserNotFoundException("Utilisateur non trouvé avec l'ID: " + id);
    }
    try {
      userRepository.deleteById(id);
      log.info("Utilisateur supprimé avec succès: {}", id);
    } catch (Exception e) {
      log.error("Erreur lors de la suppression de l'utilisateur: {}", id, e);
      throw new UserException("Erreur lors de la suppression de l'utilisateur: " + e.getMessage());
    }
  }

}

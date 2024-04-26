package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    // TODO: wire in the user repository (~ 1 line)
    @Autowired
    private UserRepository userRepository;

    @PutMapping("/user")
    public ResponseEntity<?> createUser(@RequestBody CreateUserPayload payload) {
        // TODO: Create an user entity with information given in the payload, store it in the database
        //       and return the id of the user in 200 OK response
        // Check if the payload is null
        if (payload == null) {
            System.out.println("Received a null payload for creating an user.");
            return ResponseEntity.badRequest().body("Payload cannot be null.");
        }
        try {
            // Check whether name is null
            if (payload.getName() == null || payload.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Name is required and cannot be empty.");
            }

            // Check whether email is null
            if (payload.getEmail() == null || payload.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email should not be null.");
            }
            // Check if a user with the same name and email already exists
            List<User> users = userRepository.findByNameAndEmail(payload.getName(), payload.getEmail());
            if (!users.isEmpty()) {
                // Handle as an error
                return ResponseEntity.badRequest().body("Multiple users found with the same name and email.");
            }
            User newUser = new User();
            // Assume setters correspond to payload fields
            newUser.setName(payload.getName());
            newUser.setEmail(payload.getEmail());
            newUser = userRepository.save(newUser);
            return ResponseEntity.ok("User id after creation is: " + newUser.getId());
        } catch (Exception e) {
            // Log the exception details (optional) and return an error response
            System.out.println("An error occurred while creating a user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the user.");
        }
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer userId) {
        // TODO: Return 200 OK if a user with the given ID exists, and the deletion is successful
        //       Return 400 Bad Request if a user with the ID does not exist
        //       The response body could be anything you consider appropriate
        // Check if the userId is null
        if (userId == null) {
            System.out.println("Received a null userId for deleting an user.");
            return ResponseEntity.badRequest().body("Payload cannot be null.");
        }
        try {
            // Check whether userId is valid
            if (userId <= 0) {
                return ResponseEntity.badRequest().body("Invalid user ID.");
            }
            return userRepository.findById(userId)
                    .map(user -> {
                        userRepository.delete(user);
                        return ResponseEntity.ok("User deleted successfully.");
                    })
                    .orElse(ResponseEntity.badRequest().body("User with ID: " + userId + " does not exist."));
        } catch (Exception e) {
            // Log the exception details (optional) and return an error response
            System.out.println("An error occurred while deleting a user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the user.");
        }
    }
}

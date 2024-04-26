package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class CreditCardController {

    // TODO: wire in CreditCard repository here (~1 line)
    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/credit-card")
    public ResponseEntity<?> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        // TODO: Create a credit card entity, and then associate that credit card with user with given userId
        //       Return 200 OK with the credit card id if the user exists and credit card is successfully associated with the user
        //       Return other appropriate response code for other exception cases
        //       Do not worry about validating the card number, assume card number could be any arbitrary format and length
        // Check if the payload is null
        if (payload == null) {
            System.out.println("Received a null payload for adding credit card to an user.");
            return ResponseEntity.badRequest().body("Payload cannot be null.");
        }
        try {
            // Check if userId is valid
            if (payload.getUserId() <= 0) {
                return ResponseEntity.badRequest().body("Invalid user ID.");
            }

            // Check if card number is null
            if (payload.getCardNumber() == null || payload.getCardNumber().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Card number cannot be empty.");
            }

            // Check if card issuance bank is nul
            if (payload.getCardIssuanceBank() == null || payload.getCardIssuanceBank().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Card issuance bank cannot be empty.");
            }

            Optional<User> userOptional = userRepository.findById(payload.getUserId());
            // Check if user exists, if user does not exist, return a bad request
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("User does not exist.");
            }

            // check if duplicate credit cards exist
            List<CreditCard> existingCards = creditCardRepository.findByOwnerIdAndNumberAndIssuanceBank(
                    payload.getUserId(), payload.getCardNumber(), payload.getCardIssuanceBank());
            if (!existingCards.isEmpty()) {
                return ResponseEntity.badRequest().body("Credit card already exists.");
            }

            CreditCard creditCard = new CreditCard();
            creditCard.setIssuanceBank(payload.getCardIssuanceBank());
            creditCard.setNumber(payload.getCardNumber());
            creditCard.setOwner(userOptional.get());

            creditCard = creditCardRepository.save(creditCard);
            return ResponseEntity.ok("Credit card id added to user is: " + creditCard.getId());
        } catch (Exception e) {
            // Log the exception details (optional) and return an error response
            System.out.println("An error occurred while adding credit card to a user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while adding credit card to the user.");
        }
    }

    @GetMapping("/credit-card/all/{userId}")
    public ResponseEntity<?> getAllCardOfUser(@PathVariable Integer userId) {
        // TODO: return a list of all credit card associated with the given userId, using CreditCardView class
        //       if the user has no credit card, return empty list, never return null
        // check if user is null
        if (userId == null) {
            System.out.println("Received a null userId for getting all cards of an user.");
            return ResponseEntity.badRequest().body("User id cannot be null.");
        }
        try {
            // Validate userId
            if (userId <= 0) {
                return ResponseEntity.badRequest().body("Invalid user ID.");
            }
            // Check if user exists
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.badRequest().body("User does not exist.");
            }

            //User id is guaranteed to be unique based on the way its generated
            List<CreditCard> cards = creditCardRepository.findByOwnerId(userId);
            List<CreditCardView> cardViews = cards.stream()
                    .map(card -> new CreditCardView(card.getNumber(), card.getIssuanceBank()))
                    .toList();

            return ResponseEntity.ok("All credit card information associated with the userId " + userId + " is: \n" + cardViews);
        } catch (Exception e) {
            // Log the exception details (optional) and return an error response
            System.out.println("An error occurred while getting credit cards for a user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while getting credit cards for the user.");

        }
    }

    @GetMapping("/credit-card/user-id/{creditCardNumber}")
    public ResponseEntity<?> getUserIdForCreditCard(@PathVariable String creditCardNumber) {
        // TODO: Given a credit card number, efficiently find whether there is a user associated with the credit card
        //       If so, return the user id in a 200 OK response. If no such user exists, return 400 Bad Request
        // check if credit card is null
        if (creditCardNumber == null || creditCardNumber.trim().isEmpty()) {
            System.out.println("Received a null credit card number for getting userid of an user.");
            return ResponseEntity.badRequest().body("Payload cannot be null.");
        }
        try {
            List<CreditCard> creditCards = creditCardRepository.findByNumber(creditCardNumber);
            // check if credit cards does not exist in the repository
            if (creditCards.isEmpty()) {
                return ResponseEntity.badRequest().body("No such credit card exists.");
                // for one credit card number, there could be multiple owners
            } else if (creditCards.size() > 1) {
                return ResponseEntity.badRequest().body("Duplicate credit card numbers found. Cannot retrieve a single user ID.");
            }

            return ResponseEntity.ok("User id from credit card number " + creditCardNumber + " is: " + creditCards.get(0).getOwner().getId());
        } catch (Exception e) {
            // Log the exception details (optional) and return an error response
            System.out.println("An error occurred while getting an user id for a credit card number: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while getting an user id for the credit card number.");
        }
    }

    @PostMapping("/credit-card/update-balance")
    public ResponseEntity<String> updateCreditCardBalances(@RequestBody UpdateBalancePayload[] payloads) {
        //TODO: Given a list of transactions, update credit cards' balance history.
        //      1. For the balance history in the credit card
        //      2. If there are gaps between two balance dates, fill the empty date with the balance of the previous date
        //      3. Given the payload `payload`, calculate the balance different between the payload and the actual balance stored in the database
        //      4. If the different is not 0, update all the following budget with the difference
        //      For example: if today is 4/12, a credit card's balanceHistory is [{date: 4/12, balance: 110}, {date: 4/10, balance: 100}],
        //      Given a balance amount of {date: 4/11, amount: 110}, the new balanceHistory is
        //      [{date: 4/12, balance: 120}, {date: 4/11, balance: 110}, {date: 4/10, balance: 100}]
        //      Return 200 OK if update is done and successful, 400 Bad Request if the given card number
        //        is not associated with a card.

        // Check if the payloads array itself is null or empty
        if (payloads == null || payloads.length == 0) {
            System.out.println("No payloads provided for updating credit card balances.");
            return ResponseEntity.badRequest().body("No payloads provided for updating credit card balances.");
        }
        try {
            for (UpdateBalancePayload payload : payloads) {
                if (payload == null) {
                    System.out.println("Encountered a null payload in the request.");
                    return ResponseEntity.badRequest().body("Payloads must not contain null elements.");
                }

                // Check whether card number is null
                if (payload.getCreditCardNumber() == null || payload.getCreditCardNumber().trim().isEmpty()) {
                    return ResponseEntity.badRequest().body("Card number cannot be null");
                }

                // Validate balance amount
                if (payload.getBalanceAmount() < 0) {
                    System.out.println("Negative balance amount received for credit card number: " + payload.getCreditCardNumber());
                    return ResponseEntity.badRequest().body("Balance amount cannot be negative.");
                }

                System.out.println("Processing payload for credit card number: " + payload.getCreditCardNumber());
                List<CreditCard> creditCards = creditCardRepository.findByNumber(payload.getCreditCardNumber());
                if (creditCards == null || creditCards.isEmpty()) {
                    System.out.println("Error: Credit card not found for number: " + payload.getCreditCardNumber());
                    return ResponseEntity.badRequest().body("Credit card not found for number: " + payload.getCreditCardNumber());
                }

                CreditCard card = creditCards.get(0);
                LocalDate payloadDate = payload.getBalanceDate();
                List<BalanceHistory> balanceHistories = card.getBalanceHistories();
                if (balanceHistories == null) {
                    balanceHistories = new ArrayList<>();
                    card.setBalanceHistories(balanceHistories);
                }

                // sort dates in ascending order
                balanceHistories.sort(Comparator.comparing(BalanceHistory::getDate));

                // Determine the previous date from the last history if available
                LocalDate previousDate = balanceHistories.isEmpty() ? null : balanceHistories.get(balanceHistories.size() - 1).getDate();

                // get the existing entry and filter by most recent date
                Optional<BalanceHistory> existingEntry = balanceHistories.stream()
                        .filter(b -> b.getDate().isEqual(payloadDate))
                        .reduce((first, second) -> second);


                // check for duplicates
                if (existingEntry.isPresent()) {
                    BalanceHistory history = existingEntry.get();
                    double originalBalance = history.getBalance();  // Store the original balance before making any changes
                    if (history.getBalance() != payload.getBalanceAmount()) {
                        System.out.println("Updated balance for date " + payloadDate + " from " + history.getBalance() + " to " + payload.getBalanceAmount());
                        history.setBalance(payload.getBalanceAmount());  // Update the balance
                        int index = balanceHistories.indexOf(history);
                        // Manually handling the logic to account for differences within subsequent transactions
                        if(index == -1) {
                            System.out.println("Index not found in updated history");
                            continue;
                        }
                        for (int i = index + 1; i < balanceHistories.size(); i++) {
                            BalanceHistory currentHistory = balanceHistories.get(i);
                            if (currentHistory != null) {
                                // internally calculating the difference for varying payload differences
                                double difference = payload.getBalanceAmount() - originalBalance;
                                System.out.println("Difference is: " + difference);
                                if(difference != 0.0) {
                                    System.out.println("Difference found: " + difference + ", updating following balances.");
                                    double oldBalance = currentHistory.getBalance();
                                    currentHistory.setBalance(oldBalance + difference);
                                    System.out.println("Updated balance for date " + currentHistory.getDate() + " from " + oldBalance + " to " + currentHistory.getBalance());
                                }
                                else {
                                    System.out.println("No difference found. No need to update following balances.");
                                }
                            } else {
                                System.out.println("Null history found at index: " + i);
                            }
                        }
                    }
                    else {
                        System.out.println("Duplicate balance entry for date " + payloadDate + " with the same amount ignored.");
                        continue;
                    }
                } else {
                    System.out.println("Adding new balance entry for date " + payloadDate);
                    BalanceHistory newHistory = new BalanceHistory();
                    newHistory.setDate(payloadDate);
                    newHistory.setBalance(payload.getBalanceAmount());
                    newHistory.setCreditCard(card);
                    fillGaps(balanceHistories, newHistory, previousDate);
                    card.getBalanceHistories().add(newHistory);
                    Collections.sort(card.getBalanceHistories(), Comparator.comparing(BalanceHistory::getDate));
                    updateFollowingBalances(card.getBalanceHistories(), newHistory, payload.getBalanceAmount());
                }

                creditCardRepository.save(card); // Save the card with updated/new balance history
                System.out.println("Balance history successfully updated for card number: " + payload.getCreditCardNumber());
            }

            return ResponseEntity.ok("Balances updated successfully.");
        } catch (Exception e) {
            if (e.getCause() instanceof DateTimeParseException) {
                return ResponseEntity.badRequest().body("The provided date is in an incorrect format. Please use 'YYYY-MM-DD'.");
            }
            System.out.println("An error occurred while updating the balances for the provided payload: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the balances for the provided payload.");
        }

    }

    // logic to fill the gaps if there are any empty gaps
    private void fillGaps(List<BalanceHistory> histories, BalanceHistory newHistory, LocalDate previousDate) {
        LocalDate newDate = newHistory.getDate();
        while (previousDate != null && previousDate.plusDays(1).isBefore(newDate)) {
            previousDate = previousDate.plusDays(1);
            BalanceHistory fillerHistory = new BalanceHistory();
            fillerHistory.setDate(previousDate);
            fillerHistory.setBalance(histories.isEmpty() ? 0 : histories.get(histories.size() - 1).getBalance());
            fillerHistory.setCreditCard(newHistory.getCreditCard());
            histories.add(fillerHistory);
            System.out.println("Filled gap at " + previousDate + " with balance " + fillerHistory.getBalance());
        }
    }

    // logic to update balances ensuring we only consider differences
    private void updateFollowingBalances(List<BalanceHistory> histories, BalanceHistory updatedHistory, Double difference) {
        int index = histories.indexOf(updatedHistory);
        if(index == -1) {
            System.out.println("Index not found in updated history");
            return;
        }
        if (difference != 0.0) { // Check if the difference is not zero
            System.out.println("Difference found: " + difference + ", updating following balances.");
            for (int i = index + 1; i < histories.size(); i++) {
                BalanceHistory currentHistory = histories.get(i);
                if (currentHistory != null) {
                    double oldBalance = currentHistory.getBalance();
                    currentHistory.setBalance(oldBalance + difference);
                    System.out.println("Updated balance for date " + currentHistory.getDate() + " from " + oldBalance + " to " + currentHistory.getBalance());
                } else {
                    System.out.println("Null history found at index: " + i);
                }
            }
        } else {
            System.out.println("No difference found. No need to update following balances.");
        }
    }
}

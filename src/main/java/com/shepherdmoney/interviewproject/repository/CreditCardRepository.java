package com.shepherdmoney.interviewproject.repository;

import com.shepherdmoney.interviewproject.model.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Crud repository to store credit cards
 */
@Repository("CreditCardRepo")
public interface CreditCardRepository extends JpaRepository<CreditCard, Integer> {
    List<CreditCard> findByOwnerId(int userId);

    List<CreditCard> findByNumber(String creditCardNumber);

    List<CreditCard> findByOwnerIdAndNumberAndIssuanceBank(int userId, String cardNumber, String cardIssuanceBank);
}

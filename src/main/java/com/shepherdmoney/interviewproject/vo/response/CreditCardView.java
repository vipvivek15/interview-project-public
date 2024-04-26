package com.shepherdmoney.interviewproject.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CreditCardView {

    private String number;

    private String issuanceBank;

    @Override
    public String toString() {
        return "Card Number: " + number + ", Issuance Bank: " + issuanceBank;
    }

}

package com.djulia.transactions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Objects;

@RestController
public class TransactionsController {
    private final BankingTransactionService transactionService;

    @Autowired
    public TransactionsController(BankingTransactionService transactionService) {
        this.transactionService = Objects.requireNonNull(transactionService);
    }

    @RequestMapping(value = "/accounts/withdrawals")
    public ResponseEntity<?> makeWithdrawal(@RequestBody WithdrawalRequest request ) {
        WithdrawalResult result = transactionService.withdraw(request.getAccountNumber(), request.getAmount());
        if(result.isSuccessful()){
            WithdrawalResponse body = new WithdrawalResponse(result.getUpdatedAccount().get().getBalance());
            return ResponseEntity.status(201).body(body);
        }

        WithdrawalResult.Error error = result.getError().get();
        switch (error){
            case INACTIVE_ACCOUNT:
                return ResponseEntity.status(400).body(new TransactionsController.ErrorResponse("Inactive Account. Can't withdraw"));
            case INVALID_WITHDRAWAL_AMOUNT:
                return ResponseEntity.status(400).body(new TransactionsController.ErrorResponse("Invalid withdrawal"));
            case INSUFFICIENT_FUNDS:
                return ResponseEntity.status(400).body(new TransactionsController.ErrorResponse("Insufficient funds"));
            case NO_SUCH_ACCOUNT:
                return ResponseEntity.status(400).body(
                        new TransactionsController.ErrorResponse("No account found with the id "+ request.getAccountNumber()));
        }
        //Did I catch them all?
        //Well, at least while I typed them out, IntelliJ autocompleted all my examples. That's how I realized
        //that I was missing a case for NO_SUCH_ACCOUNT. But I easily could have not noticed!
        //Talk about a leaky abstraction!
        //And these are known, reasonable cases!!!
        throw new RuntimeException("I should never get here!");
    }



    public static class WithdrawalResponse {
        private BigDecimal remainingBalance;

        public WithdrawalResponse(BigDecimal remainingBalance) {
            this.remainingBalance = remainingBalance;
        }

        public BigDecimal getRemainingBalance() {
            return remainingBalance;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            WithdrawalResponse that = (WithdrawalResponse) o;

            return remainingBalance != null ? remainingBalance.equals(that.remainingBalance) : that.remainingBalance == null;

        }

        @Override
        public int hashCode() {
            return remainingBalance != null ? remainingBalance.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "WithdrawalResponse{" +
                    "remainingBalance=" + remainingBalance +
                    '}';
        }
    }

    public static class ErrorResponse {
        private final String errorMessage;
        @JsonCreator
        public ErrorResponse(@JsonProperty("errorMessage") String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ErrorResponse that = (ErrorResponse) o;

            return errorMessage != null ? errorMessage.equals(that.errorMessage) : that.errorMessage == null;

        }

        @Override
        public String toString() {
            return "ErrorResponse{" +
                    "errorMessage='" + errorMessage + '\'' +
                    '}';
        }

        @Override
        public int hashCode() {
            return errorMessage != null ? errorMessage.hashCode() : 0;
        }
    }
}
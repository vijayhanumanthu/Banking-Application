package com.coding.exercise.bankapp.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coding.exercise.bankapp.domain.AccountInformation;
import com.coding.exercise.bankapp.domain.TransactionDetails;
import com.coding.exercise.bankapp.domain.TransferDetails;
import com.coding.exercise.bankapp.service.BankingService;
import com.coding.exercise.bankapp.service.BankingServiceImpl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final BankingService bankingService;

    public AccountController(BankingService bankingService) {
        this.bankingService = bankingService;
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountInformation> getAccount(
            @PathVariable Long accountNumber) {

        return ResponseEntity.ok(
                bankingService.findAccountByNumber(accountNumber)
        );
    }
	
    @PostMapping("/customers/{customerNumber}")
    public ResponseEntity<AccountInformation> addAccount(
            @Valid @RequestBody AccountInformation accountInformation,
            @PathVariable Long customerNumber) {

        AccountInformation account =
                bankingService.addNewAccount(accountInformation, customerNumber);

        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }
	
	@PutMapping(path = "/transfer/{customerNumber}")
	@ApiOperation(value = "Transfer funds between accounts", notes = "Transfer funds between accounts.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 500, message = "Internal Server Error") })

	public ResponseEntity<Object> transferDetails(@RequestBody TransferDetails transferDetails,
			@PathVariable Long customerNumber) {

		return bankingService.transferDetails(transferDetails, customerNumber);
	}

		@GetMapping(path = "/transactions/{accountNumber}")
	@ApiOperation(value = "Get all transactions", notes = "Get all Transactions by account number")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 500, message = "Internal Server Error") })

	public List<TransactionDetails> getTransactionByAccountNumber(@PathVariable Long accountNumber) {

		return bankingService.findTransactionsByAccountNumber(accountNumber);
	}
}

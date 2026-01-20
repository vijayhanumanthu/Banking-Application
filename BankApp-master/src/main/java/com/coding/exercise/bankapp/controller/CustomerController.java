package com.coding.exercise.bankapp.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coding.exercise.bankapp.domain.CustomerDetails;
import com.coding.exercise.bankapp.service.BankingService;
import com.coding.exercise.bankapp.service.BankingServiceImpl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final BankingService bankingService;

    public CustomerController(BankingService bankingService) {
        this.bankingService = bankingService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerDetails>> getAllCustomers() {
        return ResponseEntity.ok(bankingService.findAllCustomers());
    }

    @PostMapping
    public ResponseEntity<CustomerDetails> addCustomer(
            @Valid @RequestBody CustomerDetails customer) {

        CustomerDetails created =
                bankingService.addCustomer(customer);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

	@PutMapping(path = "/{customerNumber}")
	@ApiOperation(value = "Update customer", notes = "Update customer and any other account information associated with him.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 500, message = "Internal Server Error") })

	public ResponseEntity<Object> updateCustomer(@RequestBody CustomerDetails customerDetails,
			@PathVariable Long customerNumber) {

		return bankingService.updateCustomer(customerDetails, customerNumber);
	}
	
	@DeleteMapping(path = "/{customerNumber}")
	@ApiOperation(value = "Delete customer and related accounts", notes = "Delete customer and all accounts associated with him.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = Object.class),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 500, message = "Internal Server Error") })

	public ResponseEntity<Object> deleteCustomer(@PathVariable Long customerNumber) {

		return bankingService.deleteCustomer(customerNumber);
	}
}

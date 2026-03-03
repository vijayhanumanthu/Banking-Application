package com.coding.exercise.bankapp.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BankInformation {

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public Integer getBranchCode() {
		return branchCode;
	}

	public void setBranchCode(Integer branchCode) {
		this.branchCode = branchCode;
	}

	public AddressDetails getBranchAddress() {
		return branchAddress;
	}

	public void setBranchAddress(AddressDetails branchAddress) {
		this.branchAddress = branchAddress;
	}

	public Integer getRoutingNumber() {
		return routingNumber;
	}

	public void setRoutingNumber(Integer routingNumber) {
		this.routingNumber = routingNumber;
	}

	private String branchName;
	
	private Integer branchCode;
	
	private AddressDetails branchAddress;
	
	private Integer routingNumber;
}

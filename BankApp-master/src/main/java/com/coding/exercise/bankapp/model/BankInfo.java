package com.coding.exercise.bankapp.model;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
public class BankInfo {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="BANK_ID")
	private UUID id;
	
	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

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

	public Address getBranchAddress() {
		return branchAddress;
	}

	public void setBranchAddress(Address branchAddress) {
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
	
	@OneToOne(cascade=CascadeType.ALL)
	private Address branchAddress;
	
	private Integer routingNumber;
	
}

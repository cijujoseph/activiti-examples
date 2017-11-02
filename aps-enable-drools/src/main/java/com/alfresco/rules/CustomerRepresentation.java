package com.alfresco.rules;

import java.io.Serializable;

public class CustomerRepresentation implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String custTypCD;
	private Boolean customerRuleOutput;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCustTypCD() {
		return custTypCD;
	}
	public void setCustTypCD(String custTypCD) {
		this.custTypCD = custTypCD;
	}
	public Boolean getCustomerRuleOutput() {
		return customerRuleOutput;
	}
	public void setCustomerRuleOutput(Boolean customerRuleOutput) {
		this.customerRuleOutput = customerRuleOutput;
	}
	@Override
	public String toString() {
		return "CustomerRepresentation [name=" + name + ", custTypCD=" + custTypCD + ", customerRuleOutput="
				+ customerRuleOutput + "]";
	}
	
}
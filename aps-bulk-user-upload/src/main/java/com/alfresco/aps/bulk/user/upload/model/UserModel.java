package com.alfresco.aps.bulk.user.upload.model;

public class UserModel {
	
	private String password;
	private String firstName;
	private String lastName;
	private String email;
	private String company;
	private String userId;
	
	@Override
	public String toString() {
		return "User [password=" + password + ", firstName=" + firstName + ", lastName=" + lastName + ", email=" + email
				+ ", company=" + company + ", userId=" + userId + "]";
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}

}

package com.leap.authorization.service;

import java.util.Arrays;

public class User {

	private String userName;
	private char[] password;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public char[] getPassword() {
		return password;
	}

	public void setPassword(char[] password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "User [userName=" + userName + ", password=" + Arrays.toString(password) + "]";
	}

}

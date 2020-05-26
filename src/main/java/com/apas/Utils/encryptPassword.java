package com.apas.Utils;

public class encryptPassword {

	public static void main(String[] args) {
		PasswordUtils.encrypt("Support@123", "");
		System.out.println(PasswordUtils.encrypt("Support@123", ""));
	}
}
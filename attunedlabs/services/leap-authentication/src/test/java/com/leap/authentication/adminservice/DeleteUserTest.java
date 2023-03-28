package com.leap.authentication.adminservice;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.bean.User;
import com.leap.authentication.exception.DomainIdentificationException;
import com.leap.authentication.exception.PropertiesConfigException;
import com.leap.authentication.exception.UserProfileFetchException;
import com.leap.authentication.exception.UserRegistrationException;
import com.leap.authentication.exception.UserRepoUpdateException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;

public class DeleteUserTest {

	private static Logger logger = LoggerFactory.getLogger(DeleteUserTest.class);
	String domainName = "brandt.com";

	@Test
	public void test() throws UserProfileFetchException, DomainIdentificationException, PropertiesConfigException, UserRegistrationException, UserRepoUpdateException {
		logger.debug("Testing deleteUser...");
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		int totalNoOfUsersbefore = getAllUsers();
		adminService.deleteUser("admin" + "@" + domainName, domainName);
		int totalNoOfUsersafter = getAllUsers();
		if (totalNoOfUsersbefore == totalNoOfUsersafter)
			logger.debug("No matching Records found.");
		else
			totalNoOfUsersbefore = totalNoOfUsersbefore - 1;
		Assert.assertEquals(totalNoOfUsersbefore, totalNoOfUsersafter);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int getAllUsers() throws UserProfileFetchException, PropertiesConfigException {
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		List listAll = adminService.getAllUsers(0, 20, new String[] { domainName });
		List<User> allUsersList = (List<User>) listAll.get(0);
		int count = 0;
		for (User user : allUsersList) {
			if (!(user.getFirstName().equalsIgnoreCase("admin") || user.getLastName().equalsIgnoreCase("admin")
					|| user.getUserClaims().getEmailaddress().contains("admin"))) {
				count++;
			}
		}
		return count;
	}
}

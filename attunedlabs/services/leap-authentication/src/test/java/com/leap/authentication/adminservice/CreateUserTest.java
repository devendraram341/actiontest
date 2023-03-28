package com.leap.authentication.adminservice;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.bean.User;
import com.leap.authentication.bean.UserClaims;
import com.leap.authentication.exception.PropertiesConfigException;
import com.leap.authentication.exception.UserProfileFetchException;
import com.leap.authentication.exception.UserRepoUpdateException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;

public class CreateUserTest {
	private static Logger logger = LoggerFactory.getLogger(CreateUserTest.class);

	@Test
	public void test() throws UserRepoUpdateException, UserProfileFetchException, PropertiesConfigException {
		logger.debug("Testing createUsers...");
		
		//getting new user
		List<User> users = getUserList();
		
		//getting total number of registered users
		int totalNoOfUsersbefore = getTotalNoUsers();
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		try {
			adminService.createUsers(users, "vivo.com", "admin@vivo.com@vivo.com", "admin");
		} catch (Exception e) {
			logger.debug(e.getMessage());
			throw new UserRepoUpdateException(
					"Unable to Register User, details : " + e.getMessage() + " " + e.getCause(), e);
		}
		//getting total number of registered users
		int totalNoOfUsersafter = getTotalNoUsers();
		totalNoOfUsersbefore = totalNoOfUsersbefore + users.size();
		Assert.assertEquals(totalNoOfUsersbefore, totalNoOfUsersafter);
		logger.info("Users successFully created in WSO2 ldap-repository !");
	}

	private List<User> getUserList() {
		List<User> users = new ArrayList<>();
		User user = new User();
		UserClaims claims = new UserClaims();
		user.setTenantId("tvivo");
		user.setSiteId("svivo");
		user.setUserName("shree@vivo.com");
		user.setFirstName("shree");
		user.setLastName("shree");
		user.setTitle("shree");
		user.setPassword("shree");
		claims.setOrganization("TWO Inc");
		claims.setMobile("987654");
		claims.setTelephone("0123456");
		claims.setLocality("Btm");
		claims.setCountry("India");
		claims.setStateOrProvince("karnataka");
		claims.setStreetaddress("bangalore");
		claims.setPostalCode("560001");
		claims.setRegion("1st sector");
		claims.setEmailaddress("shree@vivo.com");
		claims.setIsActive("true");
		claims.setIsLocked("false");
		claims.setStreet("1st strt");
		user.setUserClaims(claims);
		users.add(user);
		return users;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int getTotalNoUsers() throws UserProfileFetchException, PropertiesConfigException {
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		List listAll = adminService.getAllUsers(0, 20, new String[] { "vivo.com" });
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

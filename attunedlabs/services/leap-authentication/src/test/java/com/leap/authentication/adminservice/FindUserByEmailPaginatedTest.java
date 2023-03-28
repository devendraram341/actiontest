package com.leap.authentication.adminservice;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.bean.User;
import com.leap.authentication.exception.PropertiesConfigException;
import com.leap.authentication.exception.UserProfileFetchException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;

public class FindUserByEmailPaginatedTest {
	private static Logger logger = LoggerFactory.getLogger(FindUserByEmailPaginatedTest.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void test() throws UserProfileFetchException, PropertiesConfigException {
		logger.debug("Testing findUserByEmailPaginated...");
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		List findUserByEmailPaginated = adminService.findUserByEmailPaginated("kumar@test4.com",
				"test4.com", 0, 20);
		List<User> users = (List<User>) (findUserByEmailPaginated.get(0));
		if (users.size() <= 0)
			logger.debug("No matching Records found.");
		else {
			Assert.assertEquals(1, users.size());
			Assert.assertEquals("kumar", users.get(0).getFirstName());
		}
	}

}

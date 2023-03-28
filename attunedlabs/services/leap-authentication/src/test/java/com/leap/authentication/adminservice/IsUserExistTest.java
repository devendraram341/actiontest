package com.leap.authentication.adminservice;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;

public class IsUserExistTest {
	private static Logger logger = LoggerFactory.getLogger(IsUserExistTest.class);

	@Test
	public void test() {
		logger.debug("Testing isUserExist...");
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		boolean userExist = adminService.isUserExist("kumar@test3.com", "test3.com");
		Assert.assertEquals(true, userExist);
	}

}

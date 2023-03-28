package com.leap.authentication.adminservice;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.exception.CredentialUpdateException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;

public class ResetUserPasswordTest {
	private static Logger logger = LoggerFactory.getLogger(ResetUserPasswordTest.class);

	@Test
	public void test() throws CredentialUpdateException {
		logger.debug("Testing resetUserPassword...");
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		try {
			adminService.resetUserPassword("kumar@test4.com", "newkumar", "test4.com");
		} catch (Exception e) {
			logger.debug(e.getMessage());
			e.printStackTrace();
		}
	}
}

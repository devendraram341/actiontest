package com.leap.authentication.adminservice;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.bean.AccountDetail;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;

public class RegisterNewAccountTest {
	private static Logger logger = LoggerFactory.getLogger(RegisterNewAccountTest.class);

	@Test
	public void test1() {
		logger.debug("test1ing resetUserPassword...");
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		try {
			AccountDetail accountDetails = new AccountDetail("test4", "test4", 3600);
			adminService.registerNewAccount(accountDetails);
		} catch (Exception e) {
			logger.debug(e.getMessage());
			e.printStackTrace();
		}
	}

}

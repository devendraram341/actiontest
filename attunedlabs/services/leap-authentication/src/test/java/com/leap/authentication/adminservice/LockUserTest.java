package com.leap.authentication.adminservice;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.exception.AttributesRegistrationException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;

public class LockUserTest {
	private static Logger logger = LoggerFactory.getLogger(LockUserTest.class);

	@Test
	public void test() throws AttributesRegistrationException {
		logger.debug("Testing getDomainNameByCompanyName...");
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		adminService.updateUserLock("test3.com", "kumar@test3.com", false);
		logger.debug("done");
	}

}

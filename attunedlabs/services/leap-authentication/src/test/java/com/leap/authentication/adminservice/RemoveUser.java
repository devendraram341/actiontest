package com.leap.authentication.adminservice;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.exception.AttributesRegistrationException;
import com.leap.authentication.exception.DomainIdentificationException;
import com.leap.authentication.exception.UserProfileFetchException;
import com.leap.authentication.exception.UserRepoUpdateException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;

public class RemoveUser {
	private static Logger logger = LoggerFactory.getLogger(RemoveUser.class);

	@Test
	public void test() throws AttributesRegistrationException, UserProfileFetchException, DomainIdentificationException, UserRepoUpdateException {
		logger.debug("Testing getDomainNameByCompanyName...");
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		adminService.deleteUser("kumar@test4.com", "test4.com");
		logger.debug("done");
	}

}

package com.leap.authentication.adminservice;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.bean.Domain;
import com.leap.authentication.exception.DomainRegistrationException;
import com.leap.authentication.exception.InvalidDomainReqException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;

public class RegisterNewDomainByAccountTest {
	private static Logger logger = LoggerFactory.getLogger(RegisterNewDomainByAccountTest.class);

	@Test
	public void test() throws DomainRegistrationException, InvalidDomainReqException {
		logger.debug("Testing registerNewDomainByCompany...");
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		Domain domain = new Domain();
		domain.setAdminEmail("admin@test4.com");
		domain.setAdminFirstName("admin");
		domain.setAdminLastName("admin");
		domain.setAdminPassword("admin");
		domain.setAdminUserName("admin@test4.com");
		domain.setCompany("test4");
		domain.setDomainName("test4.com");
		domain.setSiteId("stest4");
		domain.setTenantId("test4");
		adminService.registerNewDomainByAccount("test4", domain, false, null);
	}

}

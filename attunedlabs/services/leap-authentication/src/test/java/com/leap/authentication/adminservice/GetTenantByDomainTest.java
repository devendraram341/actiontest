package com.leap.authentication.adminservice;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.exception.DomainIdentificationException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;

public class GetTenantByDomainTest {
	private static Logger logger = LoggerFactory.getLogger(GetTenantByDomainTest.class);

	@Test
	public void test() throws DomainIdentificationException {
		logger.debug("Testing getTenantByDomain...");
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		String tenantByDomain = adminService.getTenantByDomain("vivo.com");
		Assert.assertEquals("tvivo", tenantByDomain);
	}

}

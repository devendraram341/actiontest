package com.leap.authentication.adminservice;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.exception.DomainIdentificationException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;

public class GetDomainByTenantAndSiteTest {
	private static Logger logger = LoggerFactory.getLogger(GetDomainByTenantAndSiteTest.class);

	@Test
	public void test() throws DomainIdentificationException {
		logger.debug("Testing getDomainByTenantAndSite...");
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		List<String> domainByTenantAndSite = adminService.getDomainByTenantAndSite("test4", "test4");
		Assert.assertEquals(domainByTenantAndSite.get(0), "test4.com");
	}
}

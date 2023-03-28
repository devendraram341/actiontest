package com.leap.authentication.adminservice;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.exception.DomainIdentificationException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;

public class GetDomainIDByDomainNameTest {
	private static Logger logger = LoggerFactory.getLogger(GetDomainIDByDomainNameTest.class);

	@Test
	public void test() throws DomainIdentificationException {
		logger.debug("Testing getDomainIDByDomainName...");
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		int domainIDByDomainName = adminService.getDomainIDByDomainName("vivo.com");
		Assert.assertEquals(56, domainIDByDomainName);
	}

}

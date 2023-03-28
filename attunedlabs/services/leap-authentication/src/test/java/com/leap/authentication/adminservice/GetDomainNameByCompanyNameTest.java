package com.leap.authentication.adminservice;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.security.exception.AccountFetchException;
import com.leap.authentication.exception.DomainIdentificationException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;

public class GetDomainNameByCompanyNameTest {
	private static Logger logger = LoggerFactory.getLogger(GetDomainNameByCompanyNameTest.class);

	@Test
	public void test() throws DomainIdentificationException, AccountFetchException {
		logger.debug("Testing getDomainNameByCompanyName...");
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		List<String> domainList = adminService.getDomainNameByAccountName("test4");
		logger.debug(domainList.toString());
		int domainsize = domainList.size();
		Assert.assertEquals(1, domainsize);
	}

}

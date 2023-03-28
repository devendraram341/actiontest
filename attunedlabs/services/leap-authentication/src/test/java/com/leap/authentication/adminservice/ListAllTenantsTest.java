package com.leap.authentication.adminservice;


import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.security.exception.AccountFetchException;
import com.leap.authentication.bean.Domain;
import com.leap.authentication.exception.DomainIdentificationException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;

public class ListAllTenantsTest {
	private static Logger logger = LoggerFactory.getLogger(ListAllTenantsTest.class);

	@Test
	public void test() throws DomainIdentificationException, AccountFetchException {
		logger.debug("Testing listAllTenants...");
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		List<Domain> listAllTenants = adminService.listAllDomains();
		int size = listAllTenants.size();
		Assert.assertEquals(19, size);
	}

}

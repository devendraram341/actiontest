package com.leap.authentication.adminservice;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.exception.DomainIdentificationException;
import com.leap.authentication.exception.TenantIdentificationException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;

public class GetTenantIdAndSiteIdByDomainTest {
	private static Logger logger = LoggerFactory.getLogger(GetTenantIdAndSiteIdByDomainTest.class);

	@Test
	public void test() throws DomainIdentificationException, TenantIdentificationException {
		logger.debug("Testing getTenantIdAndSiteIdByDomain...");
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		Map<String, String> tenantIdAndSiteIdByDomain = adminService.getTenantIdAndSiteIdByDomain("vivo.com");
		Assert.assertEquals(tenantIdAndSiteIdByDomain.get("tenantId"), "tvivo");
		Assert.assertEquals(tenantIdAndSiteIdByDomain.get("siteId"), "svivo");
	}
}

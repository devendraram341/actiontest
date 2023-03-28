package com.leap.authentication.usermgmntservice;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;

public class ValidateAccessTokenTest {
	private static Logger logger = LoggerFactory.getLogger(ValidateAccessTokenTest.class);

	@Test
	public void test() throws Exception {
		logger.debug("Testing validateAccessToken...");
		IAuthNUserMgmtService mgmtService = new AuthNUserMgmtServiceImpl();
		Map<String, String> authenticateUser = mgmtService.authenticateUser("shree@vivo.com", "shree", "vivo.com");
		boolean validateAccessToken = mgmtService.validateAccessToken(authenticateUser.get("access_token"));
		Assert.assertEquals(true, validateAccessToken);
	}
}

package com.leap.authentication.usermgmntservice;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;

public class RevokeUserOAuthTokenTest {

	private static Logger logger = LoggerFactory.getLogger(RevokeUserOAuthTokenTest.class);

	@Test
	public void test() throws Exception {
		logger.debug("Testing revokeUserOAuthToken...");
		IAuthNUserMgmtService mgmtService = new AuthNUserMgmtServiceImpl();
		Map<String, String> authenticateUser = mgmtService.authenticateUser("u004@labs.com", "u004@123", "labs.com");
		boolean revokeUserOAuthToken = mgmtService.revokeUserOAuthToken(authenticateUser.get("access_token"));
		Assert.assertEquals(true, revokeUserOAuthToken);
	}

}

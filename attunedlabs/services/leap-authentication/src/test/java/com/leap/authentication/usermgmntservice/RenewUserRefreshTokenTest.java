package com.leap.authentication.usermgmntservice;

import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;

public class RenewUserRefreshTokenTest {

	private static Logger logger = LoggerFactory.getLogger(RenewUserRefreshTokenTest.class);

	@Test
	public void test() throws Exception {
		logger.debug("Testing renewUserRefreshToken...");
		IAuthNUserMgmtService mgmtService = new AuthNUserMgmtServiceImpl();
		Map<String, String> authenticateUser = mgmtService.authenticateUser("shree@apple.com", "shree", "apple.com");
		Map<String, Object> renewUserRefreshToken = mgmtService.renewUserRefreshToken(authenticateUser.get("refresh_token"));
		logger.debug(renewUserRefreshToken.toString());
	}

}

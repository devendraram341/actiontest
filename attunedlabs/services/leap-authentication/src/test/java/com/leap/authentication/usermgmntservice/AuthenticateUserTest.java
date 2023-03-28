package com.leap.authentication.usermgmntservice;

import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;

public class AuthenticateUserTest {
	private static Logger logger = LoggerFactory.getLogger(AuthenticateUserTest.class);

	@Test
	public void test() throws Exception {
		logger.debug("Testing authenticateUser...");
		IAuthNUserMgmtService mgmtService = new AuthNUserMgmtServiceImpl();
		Map<String, String> authenticateUser = mgmtService.authenticateUser("DEFAULT_APP_appUser@internalT2.com", "(Bqqyq2H-H=XYB%", "internalT2.com");
		logger.debug(authenticateUser.toString());
	}
}

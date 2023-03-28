package com.leap.authentication.usermgmntservice;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;

public class ValidateAndUpdatePasswordTest {
	private static Logger logger = LoggerFactory.getLogger(ValidateAndUpdatePasswordTest.class);

	@Test
	public void test() throws Exception {
		logger.debug("Testing validateAndUpdatePassword...");
		IAuthNUserMgmtService mgmtService = new AuthNUserMgmtServiceImpl();
		String forgotPasswordConfmCode = mgmtService.getForgotPasswordConfmCode("shree@vivo.com", "vivo.com");
		mgmtService.validateAndUpdatePassword(forgotPasswordConfmCode, "shree@vivo.com", "vivo.com",
				"newshree");
	}

}

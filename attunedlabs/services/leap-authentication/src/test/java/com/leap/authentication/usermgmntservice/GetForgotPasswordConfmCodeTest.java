package com.leap.authentication.usermgmntservice;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;

public class GetForgotPasswordConfmCodeTest {
	private static Logger logger = LoggerFactory.getLogger(GetForgotPasswordConfmCodeTest.class);

	@Test
	public void test() throws Exception {
		logger.debug("Testing getForgotPasswordConfmCode...");
		IAuthNUserMgmtService mgmtService = new AuthNUserMgmtServiceImpl();
		String forgotPasswordConfmCode = mgmtService.getForgotPasswordConfmCode("shree@vivo.com", "vivo.com");
		logger.debug("forgotPasswordConfmCode : " +forgotPasswordConfmCode);
	}

}

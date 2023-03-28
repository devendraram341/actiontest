package com.leap.authentication.usermgmntservice;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;

public class ChangeUserPasswordTest {
	private static Logger logger = LoggerFactory.getLogger(ChangeUserPasswordTest.class);

	@Test
	public void test() throws Exception {
		logger.debug("Testing changeUserPassword...");
		IAuthNUserMgmtService mgmtService = new AuthNUserMgmtServiceImpl();
		try {
			mgmtService.changeUserPassword("shree@vivo.com", "vivo.com", "gokul", "shree");
		} catch (Exception e) {
			logger.debug(e.getMessage());
			e.printStackTrace();
		}
	}

}

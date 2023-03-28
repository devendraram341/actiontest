package com.leap.authentication.usermgmntservice;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.bean.User;
import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;

public class GetUserProfileTest {
	private static Logger logger = LoggerFactory.getLogger(GetUserProfileTest.class);

	@Test
	public void test() throws Exception {
		logger.debug("Testing getUserProfile...");
		IAuthNUserMgmtService mgmtService = new AuthNUserMgmtServiceImpl();
		User userProfile;
		try {
			userProfile = mgmtService.getUserProfile("shree@vivo.com", "vivo.com");
			System.out.println(userProfile);
			Assert.assertEquals("shree", userProfile.getLastName());
		} catch (Exception e) {
			logger.debug(e.getMessage());
			e.printStackTrace();
		}
	}
}

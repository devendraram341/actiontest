package com.leap.authentication.usermgmntservice;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.bean.User;
import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;

public class GetUserProfileByTokenTest {

	private static Logger logger = LoggerFactory.getLogger(GetUserProfileByTokenTest.class);

	@Test
	public void test() throws Exception {
		logger.debug("Testing getUserProfile...");
		IAuthNUserMgmtService mgmtService = new AuthNUserMgmtServiceImpl();
		Map<String, String> authenticateUser = mgmtService.authenticateUser("u004@labs.com", "u004@123", "labs.com");		
		User userProfile;
		try {
			userProfile = mgmtService.getUserProfile("Bearer " + authenticateUser.get("access_token"));
			System.out.println(userProfile);
			Assert.assertEquals("u005@labs.com", userProfile.getLastName());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

}

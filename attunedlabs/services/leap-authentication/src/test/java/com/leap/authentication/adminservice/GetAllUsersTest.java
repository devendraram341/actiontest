package com.leap.authentication.adminservice;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.bean.User;
import com.leap.authentication.exception.PropertiesConfigException;
import com.leap.authentication.exception.UserProfileFetchException;
import com.leap.authentication.service.IAuthNAdminService;
import com.leap.authentication.service.impl.AuthNAdminServiceImpl;

public class GetAllUsersTest {
	private static Logger logger = LoggerFactory.getLogger(GetAllUsersTest.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void test() throws UserProfileFetchException, PropertiesConfigException {
		logger.debug("Testing getAllUsers...");
		IAuthNAdminService adminService = new AuthNAdminServiceImpl();
		List listAll;
		try {
			listAll = adminService.getAllUsers(0, 20, new String[] {"test4.com"});
			List<User> users = (List<User>) (listAll.get(0));
			for (User user : users) {
				logger.debug(user.toString());
			}
			Assert.assertEquals(1, users.size());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

}

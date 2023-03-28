package com.leap.authentication.usermgmntservice;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;

public class GetAllDomainsByUserTest {
	private static Logger logger = LoggerFactory.getLogger(GetAllDomainsByUserTest.class);

	@Test
	public void test() throws Exception {
		logger.debug("Testing getAllDomainsByUser...");
		IAuthNUserMgmtService mgmtService = new AuthNUserMgmtServiceImpl();
		List<String> allDomainsByUser = mgmtService.getAllDomainsByUser("shree@fakeaccount.com");
		Assert.assertEquals(1, allDomainsByUser.size());
	}

}

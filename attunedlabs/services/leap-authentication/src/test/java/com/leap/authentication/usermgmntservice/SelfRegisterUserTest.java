package com.leap.authentication.usermgmntservice;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leap.authentication.bean.User;
import com.leap.authentication.bean.UserClaims;
import com.leap.authentication.service.IAuthNUserMgmtService;
import com.leap.authentication.service.impl.AuthNUserMgmtServiceImpl;

public class SelfRegisterUserTest {
	private static Logger logger = LoggerFactory.getLogger(SelfRegisterUserTest.class);

	@Test
	public void test() throws Exception {
		logger.debug("Testing selfRegisterUser...");
		IAuthNUserMgmtService mgmtService = new AuthNUserMgmtServiceImpl();
			mgmtService.selfRegisterUser(getUser(), "test4.com");
	}

	private User getUser() {
		User user = new User();
		UserClaims claims = new UserClaims();
		user.setTenantId("test4");
		user.setSiteId("stest4");
		user.setUserName("kumar@test4.com");
		user.setFirstName("kumar");
		user.setLastName("kumar");
		user.setTitle("kumar");
		user.setPassword("kumar");
		claims.setOrganization("TWO Inc");
		claims.setMobile("987654");
		claims.setTelephone("0123456");
		claims.setLocality("Btm");
		claims.setCountry("India");
		claims.setStateOrProvince("karnataka");
		claims.setStreetaddress("bangalore");
		claims.setPostalCode("560001");
		claims.setRegion("1st sector");
		claims.setEmailaddress("kumar@test4.com");
		claims.setIsActive("true");
		claims.setIsLocked("false");
		claims.setStreet("1st strt");
		user.setUserClaims(claims);
		return user;
	}
}

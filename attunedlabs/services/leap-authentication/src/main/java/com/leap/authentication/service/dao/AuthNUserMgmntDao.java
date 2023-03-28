package com.leap.authentication.service.dao;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.security.exception.AccountFetchException;
import com.attunedlabs.security.service.IAccountRegistryService;
import com.attunedlabs.security.service.impl.AccountRegistryServiceImpl;
import com.leap.authentication.exception.DomainIdentificationException;
import com.leap.authentication.exception.UserRegistrationException;
import com.leap.authentication.util.AuthNUtil;

public class AuthNUserMgmntDao {

	static final String ADD_NEW_USER = "insert into userdomain (username,domainid) values (?,?)";
	static final String GET_ID_BY_NAME = "select domainid from userdomain where userdomain.username = ?;";
	static final String GET_DOMAIN_BY_ID = "select domain from customersite where id = ?;";
	static final String GET_ENCRYPTED_FORM = "select encriptedform from domainadmin where domainadmin.domainid = ?;";

	private static Logger logger = LoggerFactory.getLogger(AuthNUserMgmntDao.class);

	public void addNewUserInDB(String userName, int domainId) throws UserRegistrationException {
		Connection conn = AuthNUtil.getDbConnection();
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = conn.prepareStatement(ADD_NEW_USER);
			preparedStatement.setString(1, userName.trim());
			preparedStatement.setInt(2, domainId);
			preparedStatement.execute();
		} catch (SQLException e) {
			throw new UserRegistrationException("Unable to store user in DB with domain: ", e);
		} finally {
			AuthNUtil.dbCleanUp(conn, preparedStatement);
		}
	}// ..end of the method

	public List<String> getAllDomainsByUser(String userName) throws DomainIdentificationException {
		if (AuthNUtil.isEmpty(userName)) {
			throw new DomainIdentificationException("Empty user passed to get the domains ");
		}
		Connection conn = AuthNUtil.getDbConnection();
		PreparedStatement preparedStatement = null;
		List<String> domainNames;
		try {
			preparedStatement = conn.prepareStatement(GET_DOMAIN_BY_ID);
			domainNames = getDomainNamesByUser(userName, conn, preparedStatement);
		} catch (DomainIdentificationException | SQLException e) {
			throw new DomainIdentificationException("Unable to get the domain names from user ", e);
		} finally {
			AuthNUtil.dbCleanUp(conn, preparedStatement);
		}
		return domainNames;
	}// ..end of the method

	/**
	 * // #TODO has bug rectify this
	 * 
	 * @param userName
	 * @param conn
	 * @param preparedStatement
	 * @return
	 * @throws DomainIdentificationException
	 */
	private List<String> getDomainNamesByUser(String userName, Connection conn, PreparedStatement preparedStatement)
			throws DomainIdentificationException {

		logger.debug("userName: " + userName);
		List<Integer> domainIds = getDomainIdsByuserName(userName);
		logger.debug("domainIds: " + domainIds);

		List<String> domainNames = new ArrayList<>();
		for (Integer domainId : domainIds) {
			try {

				preparedStatement.setInt(1, domainId);
				ResultSet resltStmt = preparedStatement.executeQuery();
				while (resltStmt.next()) {
					String res = resltStmt.getString(1);
					domainNames.add(res);
				}
			} catch (SQLException e) {
				throw new DomainIdentificationException("Unable to get the Domain ids from the mapped tables: ", e);
			}
		}
		return domainNames;
	}// ..end of the method

	/**
	 * 
	 * @param userName
	 * @return
	 * @throws DomainIdentificationException
	 */
	private List<Integer> getDomainIdsByuserName(String userName) throws DomainIdentificationException {
		Connection conn = AuthNUtil.getDbConnection();
		PreparedStatement preparedStatement = null;
		List<Integer> ids = new ArrayList<>();
		try {
			preparedStatement = conn.prepareStatement(GET_ID_BY_NAME);
			preparedStatement.setString(1, userName);
			ResultSet resltStmt = preparedStatement.executeQuery();
			while (resltStmt.next()) {
				int res = resltStmt.getInt(1);
				ids.add(res);
			}
		} catch (SQLException e) {
			throw new DomainIdentificationException("Unable to get the Domain ids from the mapped tables: ", e);
		} finally {
			AuthNUtil.dbCleanUp(conn, preparedStatement);
		}
		return ids;
	}// ..end of the method

	public Map<String, String> getAppDeatils(String tenantId, String siteId, String domainValue)
			throws AccountFetchException {
		try {
			IAccountRegistryService accountService = new AccountRegistryServiceImpl();
			int domainIdByDomain = accountService.getDomainIdByDomain(domainValue);
			Map<String, String> userDeatilMapObj = getUserDeatilMapObj(domainIdByDomain);
			return userDeatilMapObj;
		} catch (AccountFetchException e) {
			throw new AccountFetchException(e.getMessage());
		}

	}

	private Map<String, String> getUserDeatilMapObj(int domainIdByDomain) {
		Connection conn = AuthNUtil.getDbConnection();
		PreparedStatement preparedStatement = null;
		Map<String, String> userDeatilMapObj = new HashMap<String, String>();
		try {
			preparedStatement = conn.prepareStatement(GET_ENCRYPTED_FORM);
			preparedStatement.setInt(1, domainIdByDomain);
			ResultSet resltStmt = preparedStatement.executeQuery();
			while (resltStmt.next()) {
				String encriptedForm = resltStmt.getString(1);
				// logger.debug(" (.) getEmployeeId method AuthNUserMgmntDao
				// employeeId : " + encriptedForm);
				if (encriptedForm != null) {
					String credentials = new String(Base64.getDecoder().decode(encriptedForm),
							Charset.forName("UTF-8"));
					String[] userdeatilValues = credentials.split(":", 2);
					userDeatilMapObj.put("userName", userdeatilValues[0]);
					userDeatilMapObj.put("password", userdeatilValues[1]);
				}

			}
		} catch (SQLException e) {
			// throw new UserRegistrationException("Unable to validate the user
			// in DB with domain: ", e);
			e.printStackTrace();
		} finally {
			AuthNUtil.dbCleanUp(conn, preparedStatement);
		}
		logger.debug("userDeatilMapObj : " + userDeatilMapObj);
		return userDeatilMapObj;
	}
	
	public static void main(String[] args) throws AccountFetchException {
		AuthNUserMgmntDao dao=new AuthNUserMgmntDao();
		Map<String, String> appDeatils = dao.getAppDeatils("InternalT2, Inc.", "INTT2", "internalT2.com");
		System.out.println(appDeatils);
	}

}

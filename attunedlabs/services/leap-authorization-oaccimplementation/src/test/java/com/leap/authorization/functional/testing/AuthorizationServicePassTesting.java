package com.leap.authorization.functional.testing;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.leap.authorization.testing.util.ServiceTypeConstant;
import com.leap.authorization.testing.util.TestConstant;
import com.leap.authorization.testing.util.TestUtil;

@RunWith(OrderTestRunner.class)
public class AuthorizationServicePassTesting extends CamelSpringTestSupport {

	@EndpointInject(uri = "mock:response")
	protected MockEndpoint responseEndPoint;

	@Produce(uri = "direct:service-test-route")
	protected ProducerTemplate testRouteTemplate;

	@Override
	protected AbstractApplicationContext createApplicationContext() {
		return new ClassPathXmlApplicationContext(TestConstant.TEST_ROUTE_CONTEXT_FILE);
	}

	private void processTest(String requestType, String serviceName) throws InterruptedException {
		String requestBody = TestUtil.setRequestBody(requestType);
		String service = TestConstant.SERVICE_TYPE;
		responseEndPoint.expectedBodiesReceived(TestUtil.setExpectedBody("Ok"));
		testRouteTemplate.sendBodyAndHeader(requestBody, service, serviceName);
		responseEndPoint.assertIsSatisfied();
	}

	//@Test
	public void testCreateTenant() throws InterruptedException {
		processTest(TestConstant.TENANT, ServiceTypeConstant.TEST_TENANT);
	}// ..end of the create tenant test method

	//@Test
	public void testCreateUserResourceType() throws InterruptedException {
		processTest(TestConstant.USER_RESOURCE_TYPE, ServiceTypeConstant.TEST_RESOURCE_TYPE);
	}// ..end of the create USERS resource type test method

	//@Test
	public void testCreateRoleResourceType() throws InterruptedException {
		processTest(TestConstant.ROLE_RESOURCE_TYPE, ServiceTypeConstant.TEST_RESOURCE_TYPE);
	}// ..end of the create ROLE resource type test method

	//@Test
	public void testCreateItemResourceType() throws InterruptedException {
		processTest(TestConstant.ITEM_RESOURCE_TYPE, ServiceTypeConstant.TEST_RESOURCE_TYPE);
	}// ..end of the create ITEM resource type test method

	//@Test
	public void testCreateMenuResourceType() throws InterruptedException {
		processTest(TestConstant.MENU_RESOURCE_TYPE, ServiceTypeConstant.TEST_RESOURCE_TYPE);
	}// ..end of the create MENU resource type test method

	//@Test
	public void testRegisterUsersList() throws InterruptedException {
		processTest(TestConstant.TEST_USERS_LIST, ServiceTypeConstant.TEST_MULTIPLE_USER);
	}// ..end of the register multiple user test method

	//@Test
	public void testRegisterSingleUser() throws InterruptedException {
		processTest(TestConstant.TEST_SINGLE_USER, ServiceTypeConstant.TEST_USER);
	}// ..end of the register single user test method

	//@Test
	public void testCreateResourcesList() throws InterruptedException {
		processTest(TestConstant.TEST_RESOURCE_LIST, ServiceTypeConstant.TEST_MULTIPLE_RESOURCE);
	}// ..end of the create multiple resources test method

	//@Test
	public void testCreateResource() throws InterruptedException {
		processTest(TestConstant.TEST_SINGLE_RESOURCE, ServiceTypeConstant.TEST_RESOURCE);
	}// ..end of the create single resource test method

	//@Test
	public void testCreateMenuItem() throws InterruptedException {
		processTest(TestConstant.TEST_MENU_ITEMS_LIST, ServiceTypeConstant.TEST_MENU_ITEMS);
	}// ..end of the create menu items test method

	//@Test
	public void testRegisterRolesList() throws InterruptedException {
		processTest(TestConstant.TEST_ROLES_LIST, ServiceTypeConstant.TEST_MULTIPLE_ROLES);
	}// ..end of the register multiple role test method

	//@Test
	public void testRegisterSingleRole() throws InterruptedException {
		processTest(TestConstant.TEST_SINGLE_ROLE, ServiceTypeConstant.TEST_ROLE);
	}// ..end of the register single role test method

	//@Test
	public void testCreatePermissionForResourceType() throws InterruptedException {
		processTest(TestConstant.RESOURCE_TYPE_PERMISSION, ServiceTypeConstant.TEST_RESOURCE_TYPE_CREATE_PERMISSION);
	}// ..end of the creating permission for resource type test method

	//@Test
	public void testGrantPermissionForJohnOnInventory() throws InterruptedException {
		processTest(TestConstant.INVENTORY_JOHN_PERMISSION, ServiceTypeConstant.TEST_RESOURCE_USER_PERMISSION);
	}// ..end of the granting permission for john on inventory test method

	//@Test
	public void testGrantPermissionForJohnOnPutaway() throws InterruptedException {
		processTest(TestConstant.PUTAWAY_JOHN_PERMISSION, ServiceTypeConstant.TEST_RESOURCE_USER_PERMISSION);
	}// ..end of the granting permission for john on putaway test method

	//@Test
	public void testGrantPermissionForJohnOnShipping() throws InterruptedException {
		processTest(TestConstant.SHIPPING_JOHN_PERMISSION, ServiceTypeConstant.TEST_RESOURCE_USER_PERMISSION);
	}// ..end of the granting permission for john on shipping test method

	//@Test
	public void testGrantPermissionForJohnOnOutbound() throws InterruptedException {
		processTest(TestConstant.OUTBOUND_JOHN_PERMISSION, ServiceTypeConstant.TEST_RESOURCE_USER_PERMISSION);
	}// ..end of the granting permission for john on outbound test method

	//@Test
	public void testGrantPermissionForJohnOnPrinter() throws InterruptedException {
		processTest(TestConstant.PRINTER_JOHN_PERMISSION, ServiceTypeConstant.TEST_RESOURCE_USER_PERMISSION);
	}// ..end of the granting permission for john on printer test method

	//@Test
	public void testGrantPermissionForAdminOnInventory() throws InterruptedException {
		processTest(TestConstant.INVENTORY_ADMIN_PERMISSION, ServiceTypeConstant.TEST_RESOURCE_ROLE_PERMISSION);
	}// ..end of the granting permission for admin on inventory test method

	//@Test
	public void testGrantPermissionForAdminOnPutaway() throws InterruptedException {
		processTest(TestConstant.PUTAWAY_ADMIN_PERMISSION, ServiceTypeConstant.TEST_RESOURCE_ROLE_PERMISSION);
	}// ..end of the granting permission for admin on putaway test method

	//@Test
	public void testGrantPermissionForAdminOnOutbound() throws InterruptedException {
		processTest(TestConstant.OUTBOUND_ADMIN_PERMISSION, ServiceTypeConstant.TEST_RESOURCE_ROLE_PERMISSION);
	}// ..end of the granting permission for admin on outbound test method

	//@Test
	public void testGrantPermissionForAdminOnPrinter() throws InterruptedException {
		processTest(TestConstant.PRINTER_ADMIN_PERMISSION, ServiceTypeConstant.TEST_RESOURCE_ROLE_PERMISSION);
	}// ..end of the granting permission for admin on printer test method

	//@Test
	public void testAssociateRoleAdminToJohn() throws InterruptedException {
		processTest(TestConstant.ASSOCIATE_ROLE, ServiceTypeConstant.TEST_ROLE_ASSOCIATION);
	}// ..end of the associating role admin to john test method

	//@Test
	public void testGetPermissionForJohnOnInventory() throws InterruptedException {
		responseEndPoint.expectedBodiesReceived(TestUtil.setExpectedBody(ServiceTypeConstant.TEST_GET_USER_PERMISSION));
		testRouteTemplate.sendBodyAndHeader(null, TestConstant.SERVICE_TYPE, ServiceTypeConstant.TEST_GET_USER_PERMISSION);
		responseEndPoint.assertIsSatisfied();
	}// ..end of the getting permission for user on any resource test method

	//@Test
	public void testGetUsersPermission() throws InterruptedException {
		responseEndPoint.expectedBodiesReceived(TestUtil.setExpectedBody(ServiceTypeConstant.TEST_GET_RES_TYPE_PERMISSION));
		testRouteTemplate.sendBodyAndHeader(null, TestConstant.SERVICE_TYPE, ServiceTypeConstant.TEST_GET_RES_TYPE_PERMISSION);
		responseEndPoint.assertIsSatisfied();
	}// ..end of the getting permission for users resource type test method

	//@Test
	public void testHasUserPermission() throws InterruptedException {
		responseEndPoint.expectedBodiesReceived(TestUtil.setExpectedBody(ServiceTypeConstant.TEST_HAS_USER_PERMISSION));
		testRouteTemplate.sendBodyAndHeader(null, TestConstant.SERVICE_TYPE, ServiceTypeConstant.TEST_HAS_USER_PERMISSION);
		responseEndPoint.assertIsSatisfied();
	}// ..end of the method

	//@Test
	public void testHasRolePermission() throws InterruptedException {
		responseEndPoint.expectedBodiesReceived(TestUtil.setExpectedBody(ServiceTypeConstant.TEST_HAS_ROLE_PERMISSION));
		testRouteTemplate.sendBodyAndHeader(null, TestConstant.SERVICE_TYPE, ServiceTypeConstant.TEST_HAS_ROLE_PERMISSION);
		responseEndPoint.assertIsSatisfied();
	}// ..end of the method

	//@Test
	public void testHasUserMenuItemPermission() throws InterruptedException {
		responseEndPoint.expectedBodiesReceived(TestUtil.setExpectedBody(ServiceTypeConstant.TEST_HAS_MENU_ITEM_PERMISSION));
		testRouteTemplate.sendBodyAndHeader(null, TestConstant.SERVICE_TYPE, ServiceTypeConstant.TEST_HAS_MENU_ITEM_PERMISSION);
		responseEndPoint.assertIsSatisfied();
	}// ..end of the method

	@Test
	public void testGetAllUserMenuItem() throws InterruptedException {
		responseEndPoint.expectedBodiesReceived(TestUtil.setExpectedBody(ServiceTypeConstant.TEST_GET_ALL_USER_MENU_ITEM));
		testRouteTemplate.sendBodyAndHeader(null, TestConstant.SERVICE_TYPE, ServiceTypeConstant.TEST_GET_ALL_USER_MENU_ITEM);
		responseEndPoint.assertIsSatisfied();
	}// ..end of the getting all menu items for user test method

	@Test
	public void testGetAllRoleMenuItem() throws InterruptedException {
		responseEndPoint.expectedBodiesReceived(TestUtil.setExpectedBody(ServiceTypeConstant.TEST_GET_ALL_ROLE_MENU_ITEM));
		testRouteTemplate.sendBodyAndHeader(null, TestConstant.SERVICE_TYPE, ServiceTypeConstant.TEST_GET_ALL_ROLE_MENU_ITEM);
		responseEndPoint.assertIsSatisfied();
	}// ..end of the getting all menu items for role test method
}

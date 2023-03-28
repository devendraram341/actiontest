package com.leap.authorization.caching;

public class AuthQueries {
	static final String OACC_ROLES_QUERY = "SELECT RESCLASS2.ResourceClassName AS AccessorResourceClass,"
			+ " RID.ExternalID AS AccessorResource,RID2.ExternalID AS AccessedResource,"
			+ " RESCLASS.ResourceClassName AS AccessedResourceClass,RESCLASSPER.PermissionName AS Permission,"
			+ " DOM.DomainName As Domain FROM `OAC_Grant_ResPerm`  GRP"
			+ " JOIN OAC_ResourceExternalID RID ON GRP.AccessorResourceID = RID.ResourceID"
			+ " JOIN OAC_ResourceExternalID RID2 ON GRP.AccessedResourceID = RID2.ResourceID"
			+ " JOIN OAC_Resource RES ON GRP.AccessedResourceID =  RES.ResourceID"
			+ " JOIN OAC_Resource RES2 ON GRP.AccessorResourceID =  RES2.ResourceID"
			+ " JOIN OAC_Domain DOM ON DOM.DomainID = RES.DomainID"
			+ " JOIN oac_resourceclass RESCLASS ON RES.ResourceClassID = RESCLASS.ResourceClassID"
			+ " JOIN oac_resourceclass RESCLASS2 ON RES2.ResourceClassID = RESCLASS2.ResourceClassID"
			+ " JOIN OAC_ResourceClassPermission RESCLASSPER ON RESCLASSPER.ResourceClassID = GRP.ResourceClassID AND  RESCLASSPER.PermissionID = GRP.PermissionID"
			+ " where RESCLASS2.ResourceClassName ='role'";

	static final String OACC_USERS_QUERY = "SELECT RESCLASS2.ResourceClassName AS AccessorResourceClass,"
			+ " RID.ExternalID AS AccessorResource,RID2.ExternalID AS AccessedResource,"
			+ " RESCLASS.ResourceClassName AS AccessedResourceClass,RESCLASSPER.PermissionName AS Permission,"
			+ " DOM.DomainName As Domain FROM `OAC_Grant_ResPerm`  GRP"
			+ " JOIN OAC_ResourceExternalID RID ON GRP.AccessorResourceID = RID.ResourceID"
			+ " JOIN OAC_ResourceExternalID RID2 ON GRP.AccessedResourceID = RID2.ResourceID"
			+ " JOIN OAC_Resource RES ON GRP.AccessedResourceID =  RES.ResourceID"
			+ " JOIN OAC_Resource RES2 ON GRP.AccessorResourceID =  RES2.ResourceID"
			+ " JOIN OAC_Domain DOM ON DOM.DomainID = RES.DomainID"
			+ " JOIN oac_resourceclass RESCLASS ON RES.ResourceClassID = RESCLASS.ResourceClassID"
			+ " JOIN oac_resourceclass RESCLASS2 ON RES2.ResourceClassID = RESCLASS2.ResourceClassID"
			+ " JOIN OAC_ResourceClassPermission RESCLASSPER ON RESCLASSPER.ResourceClassID = GRP.ResourceClassID AND  RESCLASSPER.PermissionID = GRP.PermissionID"
			+ " where RESCLASS2.ResourceClassName ='users'";

	static final String OACC_USER_QUERY = "SELECT RESCLASS2.ResourceClassName AS AccessorResourceClass,"
			+ " RID.ExternalID AS AccessorResource,RID2.ExternalID AS AccessedResource,"
			+ " RESCLASS.ResourceClassName AS AccessedResourceClass,RESCLASSPER.PermissionName AS Permission,"
			+ " DOM.DomainName As Domain FROM `OAC_Grant_ResPerm`  GRP"
			+ " JOIN OAC_ResourceExternalID RID ON GRP.AccessorResourceID = RID.ResourceID"
			+ " JOIN OAC_ResourceExternalID RID2 ON GRP.AccessedResourceID = RID2.ResourceID"
			+ " JOIN OAC_Resource RES ON GRP.AccessedResourceID =  RES.ResourceID"
			+ " JOIN OAC_Resource RES2 ON GRP.AccessorResourceID =  RES2.ResourceID"
			+ " JOIN OAC_Domain DOM ON DOM.DomainID = RES.DomainID"
			+ " JOIN oac_resourceclass RESCLASS ON RES.ResourceClassID = RESCLASS.ResourceClassID"
			+ " JOIN oac_resourceclass RESCLASS2 ON RES2.ResourceClassID = RESCLASS2.ResourceClassID"
			+ " JOIN OAC_ResourceClassPermission RESCLASSPER ON RESCLASSPER.ResourceClassID = GRP.ResourceClassID AND  RESCLASSPER.PermissionID = GRP.PermissionID"
			+ " where RESCLASS2.ResourceClassName ='users' And RID.ExternalID=? AND DOM.DomainName=? ";

	static final String OACC_USER_ROLES_QUERY = "SELECT RESCLASS2.ResourceClassName AS AccessorResourceClass,"
			+ " RID.ExternalID AS AccessorResource,RID2.ExternalID AS AccessedResource,"
			+ " RESCLASS.ResourceClassName AS AccessedResourceClass,DOM.DomainName As Domain FROM OACCDB.OAC_Grant_ResPerm_Sys GRP"
			+ " JOIN OAC_ResourceExternalID RID ON GRP.AccessorResourceID = RID.ResourceID"
			+ " JOIN OAC_ResourceExternalID RID2 ON GRP.AccessedResourceID = RID2.ResourceID"
			+ " JOIN OAC_Resource RES ON GRP.AccessedResourceID =  RES.ResourceID "
			+ " JOIN OAC_Resource RES2 ON GRP.AccessorResourceID =  RES2.ResourceID"
			+ " JOIN OAC_Domain DOM ON DOM.DomainID = RES.DomainID"
			+ " JOIN oac_resourceclass RESCLASS ON RES.ResourceClassID = RESCLASS.ResourceClassID"
			+ " JOIN oac_resourceclass RESCLASS2 ON RES2.ResourceClassID = RESCLASS2.ResourceClassID where RESCLASS2.ResourceClassName='users' AND RID.ExternalID=? AND DOM.DomainName=?";

	static final String OACC_RESOURCE_CLASS_QUERY = "SELECT RESID.ExternalID AS Resource,RESCLASS.ResourceClassName AS ResourceClass,DOM.DomainName AS Domain from `OAC_ResourceExternalID` RESID"
			+ " JOIN OAC_Resource RES ON  RESID.ResourceID = RES.ResourceID"
			+ " JOIN oac_resourceclass RESCLASS ON RESCLASS.ResourceClassID = RES.ResourceClassID"
			+ " JOIN OAC_Domain DOM ON DOM.DomainID = RES.DomainID" + " where DOM.DomainName=? and RESID.ExternalID=?";
}

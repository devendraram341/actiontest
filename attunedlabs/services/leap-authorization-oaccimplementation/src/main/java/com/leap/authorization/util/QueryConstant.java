package com.leap.authorization.util;

public class QueryConstant {
	
	public static final String QUERY_FOR_EXTERNAL_ID = "select ExternalId from oac_resourceexternalid where ResourceId in"
			+ "				 (select distinct oac_r.ResourceId from oac_resource oac_r, oac_resourceclass oac_rc, oac_domain oac_d where oac_r.ResourceClassId=oac_rc.ResourceClassId "
			+ "				 and oac_r.ResourceClassId in(select ResourceClassID from oac_resourceclass where ResourceClassName='ROLE') and "
			+ "				 oac_r.DomainId in (select DomainId from oac_domain where DomainName LIKE ?) and oac_r.DomainId=oac_d.DomainID)";
	
	public static final String QUERY_FOR_FETCHING_ACCESSOR_ID = "select ExternalID from oac_resourceexternalid where ResourceID in (select oac_grs.AccessedResourceID from "
			+ "oac_grant_resperm_sys oac_grs inner join oac_resourceexternalid "
			+ "oac_re on oac_re.ResourceID = oac_grs.AccessorResourceID inner join oac_resourceclass oac_rc on oac_rc.ResourceClassID=oac_grs.ResourceClassID "
			+ "where oac_grs.AccessorResourceID =(select ResourceId from oac_resourceexternalid where ExternalId LIKE ?) "
			+ "and oac_grs.ResourceClassID in (select ResourceClassID from oac_resourceclass where ResourceClassName ='ROLE'))";
	
	public static final String QUERY_FOR_UPDATE_ROLE = "update oac_grant_resperm_sys oac_grs ,(select ResourceId from oac_resourceexternalid where ExternalId LIKE ?) "
			+ "as oac_re set oac_grs.AccessedResourceID =oac_re.ResourceId where oac_grs.AccessorResourceID in (select ResourceId from oac_resourceexternalid "
			+ "where ExternalId LIKE ?) and oac_grs.ResourceClassID in (select ResourceClassID from oac_resourceclass where ResourceClassName ='ROLE')";
	
	public static final String QUERY_FOR_DELETE_ROLE = "delete from oac_grant_resperm_sys where AccessorResourceID = "
			+ "(select ResourceId from oac_resourceexternalid where ExternalId LIKE '?) and "
			+ "ResourceClassID = (select ResourceClassID from oac_resourceclass where ResourceClassName ='ROLE')";
	
	public static final String QUERY_FOR_DELETE_RESOURCE = "delete from oac_grant_resperm where AccessorResourceID in "
			+ "(select ResourceID from oac_resourceexternalid where ExternalID LIKE ?) and "
			+ "AccessedResourceID in (select ResourceID from oac_resourceexternalid where ExternalID LIKE ?) "
			+ "and ResourceClassID in (select ResourceClassID from oac_resourceclass where ResourceClassName LIKE ?)";
	
	public static final String QUERY_FOR_DELETE_USER = "delete RID,GRP FROM oac_resourceexternalid RID  LEFT JOIN oac_grant_resperm GRP ON GRP.AccessorResourceID = RID.ResourceID where RID.ExternalID =?";


}

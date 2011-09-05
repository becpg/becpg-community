/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.admin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

// TODO: Auto-generated Javadoc
/**
 * class used to initialize the demo.
 *
 * @author querephi
 */
public class InitDemoVisitorImpl extends AbstractInitVisitorImpl implements InitVisitor{

	
	/** The Constant LOCALIZATION_DEMO_USER. */
	public static final String LOCALIZATION_DEMO_USER = "becpg.demo.firstname.user";
	
	/** The Constant LOCALIZATION_DEMO_MGR. */
	public static final String LOCALIZATION_DEMO_MGR = "becpg.demo.firstname.mgr";
	
	/** The Constant LOCALIZATION_DEMO_GROUP_SYSTEM. */
	public static final String LOCALIZATION_DEMO_GROUP_SYSTEM = "becpg.demo.lastname.system";
	
	/** The Constant LOCALIZATION_DEMO_GROUP_RD. */
	public static final String LOCALIZATION_DEMO_GROUP_RD = "becpg.demo.lastname.rd";
	
	/** The Constant LOCALIZATION_DEMO_GROUP_QUALITY. */
	public static final String LOCALIZATION_DEMO_GROUP_QUALITY = "becpg.demo.lastname.quality";
	
	/** The Constant LOCALIZATION_DEMO_GROUP_PURCHASING. */
	public static final String LOCALIZATION_DEMO_GROUP_PURCHASING = "becpg.demo.lastname.purchasing";
	
	/** The Constant LOCALIZATION_DEMO_GROUP_PRODUCTREVIEWER. */
	public static final String LOCALIZATION_DEMO_GROUP_PRODUCTREVIEWER = "becpg.demo.lastname.productreviewer";
	
	/** The Constant EMAIL. */
	public static final String EMAIL = "@becpg.fr";
	
	/** The Constant LOCALIZATION_PFX_SITE_TITLE. */
	public static final String LOCALIZATION_PFX_SITE_TITLE = "becpg.demo.site.title";
	
	/** The Constant LOCALIZATION_PFX_SITE_DESCRIPTION. */
	public static final String LOCALIZATION_PFX_SITE_DESCRIPTION = "becpg.demo.description";
	
	/** The Constant SITE_RD. */
	public static final String SITE_RD = "RD";
	
	/** The Constant SITE_QUALITY. */
	public static final String SITE_QUALITY = "Quality";
	
	/** The Constant SITE_PURCHASING. */
	public static final String SITE_PURCHASING = "Purchasing";
	
	/** The person service. */
	private PersonService personService;
	
	/** The authority service. */
	private AuthorityService authorityService;
	
	/** The site service. */
	private SiteService siteService;

	/**
	 * Sets the person service.
	 *
	 * @param personService the new person service
	 */
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	/**
	 * Sets the authority service.
	 *
	 * @param authorityService the new authority service
	 */
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	/**
	 * Sets the site service.
	 *
	 * @param siteService the new site service
	 */
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.admin.InitVisitor#visitContainer(org.alfresco.service.cmr.repository.NodeRef, java.util.Locale)
	 */
	@Override
	public void visitContainer(NodeRef nodeRef) {
		
		createSites();
		//deleteSites();
		
		createUsers();
	}
	
/**
 * Delete sites.
 */
private void deleteSites(){
		
		String [] sites = {SITE_RD, SITE_QUALITY, SITE_PURCHASING};
		
		for(String site : sites){			
			
			if(siteService.getSite(site) != null){				
				siteService.deleteSite(site);
			}
		}
}
	
	/**
	 * Creates the sites.
	 *
	 * @param locale the locale
	 */
	private void createSites(){
		
		String [] sites = {SITE_RD, SITE_QUALITY, SITE_PURCHASING};
		
		for(String site : sites){			
			
			if(siteService.getSite(site) == null){
				String siteTitle = I18NUtil.getMessage(String.format("%s.%s",  LOCALIZATION_PFX_SITE_TITLE, site));
				String siteDescription = I18NUtil.getMessage(String.format("%s.%s",  LOCALIZATION_PFX_SITE_DESCRIPTION, site));
				SiteInfo siteInfo = siteService.createSite("myPreset", site, siteTitle, siteDescription, SiteVisibility.PRIVATE);
			}			
		}			
		
		//set member ship
		siteService.setMembership(SITE_RD, PermissionService.GROUP_PREFIX + SystemGroup.RDMgr.toString(), SiteModel.SITE_CONTRIBUTOR);
		siteService.setMembership(SITE_RD, PermissionService.GROUP_PREFIX + SystemGroup.RDUser.toString(), SiteModel.SITE_CONTRIBUTOR);
		
		siteService.setMembership(SITE_QUALITY, PermissionService.GROUP_PREFIX + SystemGroup.QualityMgr.toString(), SiteModel.SITE_CONTRIBUTOR);
		siteService.setMembership(SITE_QUALITY, PermissionService.GROUP_PREFIX + SystemGroup.QualityUser.toString(), SiteModel.SITE_CONTRIBUTOR);
		
		siteService.setMembership(SITE_PURCHASING, PermissionService.GROUP_PREFIX + SystemGroup.PurchasingMgr.toString(), SiteModel.SITE_CONTRIBUTOR);
		siteService.setMembership(SITE_PURCHASING,PermissionService.GROUP_PREFIX +  SystemGroup.PurchasingUser.toString(), SiteModel.SITE_CONTRIBUTOR);
	}
	
	/**
	 * Creates the users.
	 *
	 */
	private void createUsers(){
		
		String user = I18NUtil.getMessage(LOCALIZATION_DEMO_USER);
		String mgr = I18NUtil.getMessage(LOCALIZATION_DEMO_MGR);
		
		String groupSystem = I18NUtil.getMessage(LOCALIZATION_DEMO_GROUP_SYSTEM);
		String groupRD = I18NUtil.getMessage(LOCALIZATION_DEMO_GROUP_RD);
		String groupQuality = I18NUtil.getMessage(LOCALIZATION_DEMO_GROUP_QUALITY);
		String groupPurchasing = I18NUtil.getMessage(LOCALIZATION_DEMO_GROUP_PURCHASING);
		String groupProductReviewer = I18NUtil.getMessage(LOCALIZATION_DEMO_GROUP_PRODUCTREVIEWER);
				
		//SystemMgr		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		String firstName = mgr;
		String lastName = groupSystem;
		String userName = firstName + "." + lastName;
		if(!personService.personExists(userName)){
			logger.debug("Create person: " + userName);
	        properties.put(ContentModel.PROP_USERNAME, userName);
	        properties.put(ContentModel.PROP_FIRSTNAME, firstName);
	        properties.put(ContentModel.PROP_LASTNAME, lastName);
	        properties.put(ContentModel.PROP_EMAIL, userName + EMAIL);
	        personService.createPerson(properties);        
	        authorityService.addAuthority(authorityService.getName(AuthorityType.GROUP, SystemGroup.SystemMgr.toString()), userName);
		}
		
		//RDMgr		
		properties = new HashMap<QName, Serializable>();
		firstName = mgr;
		lastName = groupRD;
		userName = firstName + "." + lastName;
		if(!personService.personExists(userName)){
			logger.debug("Create person: " + userName);
	        properties.put(ContentModel.PROP_USERNAME, userName);
	        properties.put(ContentModel.PROP_FIRSTNAME, firstName);
	        properties.put(ContentModel.PROP_LASTNAME, lastName);
	        properties.put(ContentModel.PROP_EMAIL, userName + EMAIL);
	        personService.createPerson(properties);        
	        authorityService.addAuthority(authorityService.getName(AuthorityType.GROUP, SystemGroup.RDMgr.toString()), userName);
		}
        
        //QualityMgr		
        properties = new HashMap<QName, Serializable>();
		firstName = mgr;
		lastName = groupQuality;
		userName = firstName + "." + lastName;
		if(!personService.personExists(userName)){
			logger.debug("Create person: " + userName);
	        properties.put(ContentModel.PROP_USERNAME, userName);
	        properties.put(ContentModel.PROP_FIRSTNAME, firstName);
	        properties.put(ContentModel.PROP_LASTNAME, lastName);
	        properties.put(ContentModel.PROP_EMAIL, userName + EMAIL);
	        personService.createPerson(properties);        
	        authorityService.addAuthority(authorityService.getName(AuthorityType.GROUP, SystemGroup.QualityMgr.toString()), userName);
		}
        //PurchasingMgr		
        properties = new HashMap<QName, Serializable>();
		firstName = mgr;
		lastName = groupPurchasing;
		userName = firstName + "." + lastName;
		if(!personService.personExists(userName)){
			logger.debug("Create person: " + userName);
			properties.put(ContentModel.PROP_USERNAME, userName);
	        properties.put(ContentModel.PROP_FIRSTNAME, firstName);
	        properties.put(ContentModel.PROP_LASTNAME, lastName);
	        properties.put(ContentModel.PROP_EMAIL, userName + EMAIL);
	        personService.createPerson(properties);        
	        authorityService.addAuthority(authorityService.getName(AuthorityType.GROUP, SystemGroup.PurchasingMgr.toString()), userName);
		}
		
        //ProductReviewerMgr		
        properties = new HashMap<QName, Serializable>();
		firstName = mgr;
		lastName = groupProductReviewer;
		userName = firstName + "." + lastName;
		if(!personService.personExists(userName)){
			logger.debug("Create person: " + userName);
			properties.put(ContentModel.PROP_USERNAME, userName);
	        properties.put(ContentModel.PROP_FIRSTNAME, firstName);
	        properties.put(ContentModel.PROP_LASTNAME, lastName);
	        properties.put(ContentModel.PROP_EMAIL, userName + EMAIL);
	        personService.createPerson(properties);        
	        authorityService.addAuthority(authorityService.getName(AuthorityType.GROUP, SystemGroup.ProductReviewer.toString()), userName);
		}
      	//RDUser
		properties = new HashMap<QName, Serializable>();
		firstName = user;
		lastName = groupRD;
		userName = firstName + "." + lastName;
		if(!personService.personExists(userName)){
			logger.debug("Create person: " + userName);
	        properties.put(ContentModel.PROP_USERNAME, userName);
	        properties.put(ContentModel.PROP_FIRSTNAME, firstName);
	        properties.put(ContentModel.PROP_LASTNAME, lastName);
	        properties.put(ContentModel.PROP_EMAIL, userName + EMAIL);
	        personService.createPerson(properties);        
	        authorityService.addAuthority(authorityService.getName(AuthorityType.GROUP, SystemGroup.RDUser.toString()), userName);
		}
		
        //QualityUser
        properties = new HashMap<QName, Serializable>();
		firstName = user;
		lastName = groupQuality;
		userName = firstName + "." + lastName;
		if(!personService.personExists(userName)){
			logger.debug("Create person: " + userName);
			properties.put(ContentModel.PROP_USERNAME, userName);
	        properties.put(ContentModel.PROP_FIRSTNAME, firstName);
	        properties.put(ContentModel.PROP_LASTNAME, lastName);
	        properties.put(ContentModel.PROP_EMAIL, userName + EMAIL);
	        personService.createPerson(properties);        
	        authorityService.addAuthority(authorityService.getName(AuthorityType.GROUP, SystemGroup.QualityUser.toString()), userName);
		}
		
        //PurchasingUser
        properties = new HashMap<QName, Serializable>();
		firstName = user;
		lastName = groupPurchasing;
		userName = firstName + "." + lastName;
		if(!personService.personExists(userName)){
			logger.debug("Create person: " + userName);
			properties.put(ContentModel.PROP_USERNAME, userName);
	        properties.put(ContentModel.PROP_FIRSTNAME, firstName);
	        properties.put(ContentModel.PROP_LASTNAME, lastName);
	        properties.put(ContentModel.PROP_EMAIL, userName + EMAIL);
	        personService.createPerson(properties);        
	        authorityService.addAuthority(authorityService.getName(AuthorityType.GROUP, SystemGroup.PurchasingUser.toString()), userName);
		}
	
	}

}

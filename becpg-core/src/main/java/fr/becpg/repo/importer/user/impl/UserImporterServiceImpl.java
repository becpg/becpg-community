package fr.becpg.repo.importer.user.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.common.csv.CSVReader;
import fr.becpg.repo.importer.ImporterException;
import fr.becpg.repo.importer.impl.ImportHelper;
import fr.becpg.repo.importer.user.UserImporterService;
import fr.becpg.repo.mail.BeCPGMailService;

/**
 * 
 * @author matthieu
 *  Csv Format:
 *  cm:lastName";"cm:firstName";"cm:email";"cm:telephone";"cm:organization";"username";"password";"memberships";"groups";"notify"
 */
public class UserImporterServiceImpl implements UserImporterService {
	
	private static Log logger = LogFactory.getLog(UserImporterService.class);

	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String NOTIFY = "notify";
	public static final String MEMBERSHIPS = "memberships";
	public static final String GROUPS = "groups";
	
	/** The Constant SEPARATOR. */
	private static final char SEPARATOR = ';';


	protected static final String FIELD_SEPARATOR = "\\|";	
	
	protected static final String PATH_SEPARATOR = "\\/";

	protected static final String DEFAULT_PRESET = "site-dashboard";	
	
	
	private NodeService nodeService;
	
	private ContentService contentService;
	
	private SiteService siteService;

	private AuthorityService authorityService;
	
	private BeCPGMailService beCPGMailService;
	

	private MutableAuthenticationService authenticationService;

	private PersonService personService;
	
	private NamespacePrefixResolver namespacePrefixResolver;

	public void setNamespacePrefixResolver(
			NamespacePrefixResolver namespacePrefixResolver) {
		this.namespacePrefixResolver = namespacePrefixResolver;
	}

	

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}



	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	
	public void setBeCPGMailService(BeCPGMailService beCPGMailService) {
		this.beCPGMailService = beCPGMailService;
	}



	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}



	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}


	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}


	public void setAuthenticationService(
			MutableAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}



	public void importUser(NodeRef nodeRef) throws ImporterException {
		if (nodeRef==null || !nodeService.exists(nodeRef)) {
			throw new ImporterException("Invalid parameter");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Node exists : " + nodeRef.getId());
		}

	
		
		ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
		InputStream is = null;
		try{
			is = reader.getContentInputStream();
			

			if (logger.isDebugEnabled()) {
				logger.debug("Reading Import File");
			}
			Charset charset = ImportHelper.guestCharset(is);
			if(logger.isDebugEnabled()){
				logger.debug("reader.getEncoding() : " + reader.getEncoding());
				logger.debug("finder.getEncoding() : " + charset );
			}
			
			
			proccessUpload(is, (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME),charset);
			
		
			
		} catch (Exception e) {
			throw new ImporterException("Invalid content",e);
		} finally {
			try {
				if(is!=null){
					is.close();
				}
			}catch (Exception e) {}
		}
		
	}



	private void proccessUpload(InputStream input, String filename, Charset charset) throws IOException {
	            if (filename != null && filename.length() > 0)
	            {
	                if (filename.endsWith(".csv"))
	                {
	                    processCSVUpload(input,charset);
	                    return;
	                }
	                if (filename.endsWith(".xls"))
	                {
	                    processXLSUpload(input);
	                    return;
	                }
	                if (filename.endsWith(".xlsx"))
	                {
	                    processXLSXUpload(input);
	                    return;
	                }
	            }
	            // If in doubt, assume it's probably a .csv
	            processCSVUpload(input,charset);
		
	}



	private void processXLSXUpload(InputStream input) {
		 logger.info("Not wet implemented");
	}



	private void processXLSUpload(InputStream input) {
		 logger.info("Not wet implemented");
	}
	
	private void processCSVUpload(InputStream input, Charset charset) throws IOException {
		
		CSVReader csvReader = new CSVReader(new InputStreamReader(input,charset), SEPARATOR);
	
			String[] splitted = null;
			boolean isFirst = true;
		    Map<String,Integer> headers = new HashMap<String, Integer>();
			while ((splitted = csvReader.readNext())!=null) {
					try {
						if(isFirst){
							headers = processHeaders(splitted);
							isFirst = false;
						} else if(splitted.length == headers.size()) {
							processRow(headers, splitted);
						}
					} catch (Exception e) {
						logger.warn(e,e);
				    }

		           
			}
		
		
	}



	private void processRow(final Map<String,Integer> headers, final String[] splitted) {
		
		final String username = splitted[headers.get(USERNAME)];
		final String password = splitted[headers.get(PASSWORD)];
		
		 AuthenticationUtil.runAs(new RunAsWork<Object>()
			        {
			            public Object doWork() throws Exception
			            {
							
							 if (authenticationService.authenticationExists(username) == false)
					         {
					                // create user
					                authenticationService.createAuthentication(username,
					                		password.toCharArray());
					          
					         }
							
							if(!personService.personExists(username)){
							
							    PropertyMap personProps = new PropertyMap();
							 
							    personProps.put(ContentModel.PROP_USERNAME, username);
					           
					            for(String key : headers.keySet()){
					            	if(isPropQname(key) && 
					            			!splitted[headers.get(key)].isEmpty()){
					            		QName prop = QName.resolveToQName(namespacePrefixResolver, key);
					            		String value = splitted[headers.get(key)];
					            		
					            		logger.debug("Adding : "+prop+" "+value);
					            		 personProps.put(prop,value);
					            	}
					            	
					            }
					            
					           NodeRef person = personService.createPerson(personProps);
					            
					            
				                if(headers.containsKey(NOTIFY)
				                		&& Boolean.parseBoolean(splitted[headers.get(NOTIFY)])){
				                	
				                	sendMail(person,username,password);
				                }
				                
					
					          if(headers.containsKey(GROUPS)){
					            	String[] groups = splitted[headers.get(GROUPS)].split(FIELD_SEPARATOR);
					            	for (int i = 0; i < groups.length; i++) {
										String[] grp = groups[i].split(PATH_SEPARATOR);
										String currGroup = null;
					            		for (int j = 0; j < grp.length; j++) {
					            			String tmp = grp[j];
					            			if(tmp!=null && !tmp.isEmpty()){
					            				if(!authorityService.authorityExists(
					            						authorityService.getName(AuthorityType.GROUP, tmp))){
							            			tmp = authorityService.createAuthority(
							            					AuthorityType.GROUP,tmp);
							            			logger.debug("Create group : "+tmp);
							            			if(currGroup!=null && ! currGroup.isEmpty()){
							            				logger.debug("Add group  "+tmp+" to "+currGroup);
							            				authorityService.addAuthority(currGroup,tmp );
							            					
							            			} 
					            				} else {
					            					tmp = authorityService.getName(AuthorityType.GROUP, tmp);
					            				}
						            			currGroup = tmp;
					            			}
										}
					            		
					            		if(currGroup!=null && !currGroup.isEmpty()){
					            			logger.debug("Add user  "+username+" to "+currGroup);
					            			
					            			
					            			authorityService.addAuthority(currGroup, username);
					            		}
					            		
									}
					            }
							
					          
							} else {
								logger.info("User "+ username +" already exist");
							}
					            
							return null;
            
		}

						 }, AuthenticationUtil.getSystemUserName());
            
            
	
		 if(headers.containsKey(MEMBERSHIPS)){
			 AuthenticationUtil.runAs(new RunAsWork<Object>()
				        {
				            public Object doWork() throws Exception
				            {
					            String[] memberships = splitted[headers.get(MEMBERSHIPS)].split(FIELD_SEPARATOR);
					            for (int i = 0; i < memberships.length; i++) {
					           
					            	String[] sites = memberships[i].split("_"); 
					            	final String siteName = formatSiteName(sites[0]);
					            	final String role = formatRole(sites[1]);
					            	if(logger.isDebugEnabled()){
				            			logger.debug("Adding role "+role+ " to "+username+ " on site "+siteName);
				            		}
					            	if(siteService.getSite(siteName)!=null){
					            		siteService.setMembership(siteName,  username ,role );
					            	} else {
					            		logger.debug("Site "+siteName+" doesn't exist.");
					            		
					            		SiteInfo siteInfo = siteService.createSite(DEFAULT_PRESET, siteName, sites[0], "", SiteVisibility.PUBLIC);
					            		//ISSUE ALF-4771
					            		try {
					            			logger.debug("Due to issue ALF-4771 we should call Share webscript to enable site");
						            		URL url = new URL("http://localhost:8080/share/service/modules/enable-site?url="+siteInfo.getShortName()+"&preset="+DEFAULT_PRESET+"");
						            		InputStream in = url.openStream();
						            		if(in!=null){
						            			in.close();
						            		}
						            	
					            		} catch (Exception e) {
											logger.error("Unable to enable site",e);
										}
					            		
					            		siteService.setMembership(siteInfo.getShortName(),  username ,role );
					            			
					            	}
					            	
								}
					            	return null;
				            }
				        }, "admin");

            }
		
	}
	
	
	private void sendMail(NodeRef person, String username, String password) {
		logger.debug("Notify user "+nodeService.getProperty(person,ContentModel.PROP_FIRSTNAME));
		beCPGMailService.sendMailNewUser(person,username,password);
	}



	private boolean isPropQname(String key) {
		return !(GROUPS.equals(key)
				|| MEMBERSHIPS.equals(key)
				|| PASSWORD.equals(key)
				|| USERNAME.equals(key)
				|| NOTIFY.equals(key));
	}



	private Map<String, Integer> processHeaders(String[] splitted) throws ImporterException {
		Map<String,Integer> headers = new HashMap<String, Integer>();
		for (int i = 0; i < splitted.length; i++) {
			logger.debug("Adding header: "+splitted[i]);
			headers.put(splitted[i], new Integer(i));
		}
		verifyHeaders(headers);
		return headers;
	}



	private void verifyHeaders(Map<String, Integer> headers) throws ImporterException{
		if(!headers.containsKey(USERNAME)
				&& headers.size()<4){
			throw new ImporterException("Invalid headers");
		}
	}



	private String formatRole(String role) {
		if(role.trim().equalsIgnoreCase("Contributor")){
			return SiteModel.SITE_CONTRIBUTOR;
		} else if(role.trim().equalsIgnoreCase("Collaborator")){
			return SiteModel.SITE_COLLABORATOR;
		} else if(role.trim().equalsIgnoreCase("Manager")){
			return SiteModel.SITE_MANAGER;
		} 
		
		return SiteModel.SITE_CONSUMER;
	}



	private String formatSiteName(String siteName) {
		return siteName.replaceAll(" ", "");
	}

}

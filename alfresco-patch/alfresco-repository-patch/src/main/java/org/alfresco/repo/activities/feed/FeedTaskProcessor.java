/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.activities.feed;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.repo.activities.post.lookup.PostLookup;
import org.alfresco.repo.domain.activities.ActivitiesDAO;
import org.alfresco.repo.domain.activities.ActivityFeedDAO;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.domain.activities.ActivityPostEntity;
import org.alfresco.repo.domain.activities.FeedControlEntity;
import org.alfresco.repo.template.ISO8601DateFormatMethod;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.JSONtoFmModel;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Base64;

import freemarker.cache.URLTemplateLoader;
import freemarker.core.TemplateClassResolver;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;

/**
 * Responsible for processing the individual task
 *
 * @author matthieu
 */
public abstract class FeedTaskProcessor
{
    private static final Log logger = LogFactory.getLog(FeedTaskProcessor.class);
    
    /** Constant <code>FEED_FORMAT_JSON="json"</code> */
    public static final String FEED_FORMAT_JSON = "json";
    /** Constant <code>FEED_FORMAT_ATOMENTRY="atomentry"</code> */
    public static final String FEED_FORMAT_ATOMENTRY = "atomentry";
    /** Constant <code>FEED_FORMAT_HTML="html"</code> */
    public static final String FEED_FORMAT_HTML = "html";
    /** Constant <code>FEED_FORMAT_RSS="rss"</code> */
    public static final String FEED_FORMAT_RSS = "rss";
    /** Constant <code>FEED_FORMAT_TEXT="text"</code> */
    public static final String FEED_FORMAT_TEXT = "text";
    /** Constant <code>FEED_FORMAT_XML="xml"</code> */
    public static final String FEED_FORMAT_XML = "xml";
    
    private static final String URL_SERVICE_SITES     = "/api/sites";
    private static final String URL_MEMBERSHIPS       = "/memberships";
    
    private static final String URL_SERVICE_TEMPLATES = "/api/activities/templates";
    private static final String URL_SERVICE_TEMPLATE  = "/api/activities/template";
    
    private final Map<String, Configuration> freemarkerConfigs = new HashMap<>();

    private boolean userNamesAreCaseSensitive = false;
    
    /**
     * <p>Setter for the field <code>userNamesAreCaseSensitive</code>.</p>
     *
     * @param userNamesAreCaseSensitive a boolean
     */
    public void setUserNamesAreCaseSensitive(boolean userNamesAreCaseSensitive)
    {
        this.userNamesAreCaseSensitive = userNamesAreCaseSensitive;
    }
    
    /**
     * <p>process.</p>
     *
     * @param jobTaskNode a int
     * @param minSeq a long
     * @param maxSeq a long
     * @param ctx a {@link org.alfresco.repo.activities.feed.RepoCtx} object
     * @throws java.lang.Exception if any.
     */
    public void process(int jobTaskNode, long minSeq, long maxSeq, RepoCtx ctx) throws Exception
    {
        long startTime = System.currentTimeMillis();
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Process: jobTaskNode '" + jobTaskNode + "' from seq '" + minSeq + "' to seq '" + maxSeq + "' on this node from grid job.");
        }
        
        ActivityPostEntity selector = new ActivityPostEntity();
        selector.setJobTaskNode(jobTaskNode);
        selector.setMinId(minSeq);
        selector.setMaxId(maxSeq);
        selector.setStatus(ActivityPostEntity.STATUS.POSTED.toString());
        
        List<ActivityPostEntity> activityPosts = null;
        int totalGenerated = 0;
        
        try
        {
            activityPosts = selectPosts(selector);
            
            if (logger.isDebugEnabled()) { logger.debug("Process: " + activityPosts.size() + " activity posts"); }
            
            // local caches for this run of activity posts
            Map<String, Set<String>> siteConnectedUsers = new HashMap<String, Set<String>>();                                 // site -> site members
            Map<Pair<String, String>, Set<String>> followerConnectedUsers = new HashMap<Pair<String, String>, Set<String>>(); // user -> followers
            Map<Pair<String, String>, Boolean> canUserReadSite = new HashMap<Pair<String, String>, Boolean>();                // <user, site> -> true/false (note: used when following, implied as true for site members)
            Map<String, List<FeedControlEntity>> userFeedControls = new HashMap<String, List<FeedControlEntity>>();
            
            List<String> fmTemplates = Arrays.asList(new String[]{"activities/org/alfresco/generic.json.ftl"});
            
            // for each activity post ...
            for (ActivityPostEntity activityPost : activityPosts)
            {
                String postingUserId = activityPost.getUserId();
                String activityType = activityPost.getActivityType();
                
                if (fmTemplates.size() == 0)
                {
                    logger.error("Skipping activity post " + activityPost.getId() + " since no specific/generic templates for activityType: " + activityType );
                    updatePostStatus(activityPost.getId(), ActivityPostEntity.STATUS.ERROR);
                    continue;
                }
                
                Map<String, Object> model = null;
                try
                {
                    model = JSONtoFmModel.convertJSONObjectToMap(activityPost.getActivityData());
                }
                catch(JSONException je)
                {
                    logger.error("Skipping activity post " + activityPost.getId() + " due to invalid activity data: ", je);
                    updatePostStatus(activityPost.getId(), ActivityPostEntity.STATUS.ERROR);
                    continue;
                }
                
                String nodeRefStr = (String) model.get(PostLookup.JSON_NODEREF);
                try
                {
                    // If a nodeRef is present, then it must be valid.
                    if (nodeRefStr != null)
                    {
                        // Attempt to create a nodeRef, making use of the constructor's validation.
                        new NodeRef(nodeRefStr);
                    }
                }
                catch (InvalidNodeRefException e)
                {
                    logger.error("Skipping activity post " + activityPost.getId() +
                                " due to invalid nodeRef: " + nodeRefStr);
                    updatePostStatus(activityPost.getId(), ActivityPostEntity.STATUS.ERROR);
                    continue;
                }
                
                // note: for MT share, site id should already be mangled - in addition to extra tenant domain info
                
                String thisSite = activityPost.getSiteNetwork();
                String tenantDomain = (String)model.get(PostLookup.JSON_TENANT_DOMAIN);
                
                if (thisSite != null)
                {
                    if (tenantDomain != null)
                    {
                        thisSite = getTenantName(thisSite, tenantDomain);
                    }
                    else
                    {
                        // for backwards compatibility
                        tenantDomain = getTenantDomain(thisSite);
                    }
                }
                if (tenantDomain == null)
                {
                    tenantDomain = TenantService.DEFAULT_DOMAIN;
                }
                
                model.put(ActivityFeedEntity.KEY_ACTIVITY_FEED_TYPE, activityPost.getActivityType());
                model.put(ActivityFeedEntity.KEY_ACTIVITY_FEED_SITE, thisSite);
                model.put("userId", activityPost.getUserId());
                model.put("id", activityPost.getId());
                model.put("date", activityPost.getPostDate()); // post date rather than time that feed is generated
                model.put("xmldate", new ISO8601DateFormatMethod());
                model.put("repoEndPoint", ctx.getRepoEndPoint());
                
                // Get recipients of this post
                Set<String> recipients = null;
                try
                {
                    recipients = getRecipients(ctx, thisSite, activityPost.getUserId(), tenantDomain, siteConnectedUsers, followerConnectedUsers, canUserReadSite);
                }
                catch (Exception e)
                {
                	Throwable validCause = ExceptionStackUtil.getCause(e, RetryingTransactionHelper.RETRY_EXCEPTIONS);
        			if (validCause != null) {
        				throw (RuntimeException) validCause;
        			}
                    logger.trace("Skipping activity post " + activityPost.getId() + " since failed to get recipients  for user  "+postingUserId+" :" , e);
                    updatePostStatus(activityPost.getId(), ActivityPostEntity.STATUS.ERROR);
                    continue;
                }
                
                try 
                { 
                    startTransaction();
                    
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Process: " + recipients.size() + " candidate connections for activity post " + activityPost.getId());
                    }
                    
                    int excludedConnections = 0;
                    
                    for (String recipient : recipients)
                    {
                        List<FeedControlEntity> feedControls = null;
                        if (! recipient.equals(""))
                        {
                            // Get user's feed controls
                            feedControls = userFeedControls.get(recipient);
                            if (feedControls == null)
                            {
                                feedControls = getFeedControls(recipient);
                                userFeedControls.put(recipient, feedControls);
                            }
                        }
                        
                        // filter based on opt-out feed controls (if any)
                        if (! acceptActivity(activityPost, feedControls))
                        {
                            excludedConnections++;
                        }
                        else
                        {
                            // node read permission check (if nodeRef is present)
                            if (! canRead(ctx, recipient, model))
                            {
                                excludedConnections++;
                                continue;
                            }
                            
                            for (String fmTemplate : fmTemplates)
                            {
                                String formatFound = FeedTaskProcessor.FEED_FORMAT_JSON;
                                
                                ActivityFeedEntity feed = new ActivityFeedEntity();
                                
                                // Generate activity feed summary 
                                //MNT-9104 If username contains uppercase letters the action of joining a site will not be displayed in "My activities" 
                                if (! userNamesAreCaseSensitive)
                                {
                                    recipient = recipient.toLowerCase();
                                    postingUserId = postingUserId.toLowerCase();
                                }
                                feed.setFeedUserId(recipient);
                                feed.setPostUserId(postingUserId);
                                feed.setActivityType(activityType);
                                
                                String activitySummary = null;
                                    // allows JSON to simply pass straight through
                                    activitySummary = activityPost.getActivityData();
                                
                                if (! activitySummary.equals(""))
                                {
                                    if (activitySummary.length() > ActivityFeedDAO.MAX_LEN_ACTIVITY_SUMMARY)
                                    {
                                        logger.warn("Skip feed entry (activity post " + activityPost.getId() + ") since activity summary - exceeds " + ActivityFeedDAO.MAX_LEN_ACTIVITY_SUMMARY + " chars: " + activitySummary);
                                    }
                                    else
                                    {
                                        feed.setActivitySummary(activitySummary);
                                        feed.setSiteNetwork(thisSite);
                                        feed.setAppTool(activityPost.getAppTool());
                                        feed.setPostDate(activityPost.getPostDate());
                                        feed.setPostId(activityPost.getId());
                                        feed.setFeedDate(new Date());
                                        
                                        // Insert activity feed
                                        insertFeedEntry(feed); // ignore returned feedId
                                        
                                        totalGenerated++;
                                    }
                                }
                                else
                                {
                                    if (logger.isDebugEnabled())
                                    {
                                        logger.debug("Empty template result for activityType '" + activityType + "' using format '" + formatFound + "' hence skip feed entry (activity post " + activityPost.getId() + ")");
                                    }
                                }
                            }
                        }
                    }
                    
                    updatePostStatus(activityPost.getId(), ActivityPostEntity.STATUS.PROCESSED);
                    
                    commitTransaction();
                    
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Processed: " + (recipients.size() - excludedConnections) + " connections for activity post " + activityPost.getId() + " (excluded " + excludedConnections + ")");
                    }
                } 
                finally 
                { 
                    endTransaction();
                } 
            }
        }
        catch(SQLException se)
        {
            logger.error(se);
            throw se;
        }
        finally
        {
            int postCnt = activityPosts == null ? 0 : activityPosts.size();
            
            // TODO i18n info message
            StringBuilder sb = new StringBuilder();
            sb.append("Generated ").append(totalGenerated).append(" activity feed entr").append(totalGenerated == 1 ? "y" : "ies");
            sb.append(" for ").append(postCnt).append(" activity post").append(postCnt != 1 ? "s" : "").append(" (in ").append(System.currentTimeMillis() - startTime).append(" msecs)");
            logger.info(sb.toString());
        }
    }
    
    private Set<String> getRecipients(RepoCtx ctx, String siteId, String postUserId, String tenantDomain,
                                      Map<String, Set<String>> siteConnectedUsers, Map<Pair<String, String>, Set<String>> followerConnectedUsers, Map<Pair<String, String>, Boolean> canUserReadSite) throws Exception
    {
        // Recipients of this post
        Set<String> recipients = new HashSet<String>();
        
        // Add site members to recipient list
        if ((null != siteId) && (siteId.length() > 0))
        {
            // Get the members of this site - save hammering the repository by reusing cached site members
            Set<String> connectedUsers = siteConnectedUsers.get(siteId);
            if (connectedUsers == null)
            {
                try
                {
                    // Repository callback to get site members
                    connectedUsers = getSiteMembers(ctx, siteId, tenantDomain);
                    connectedUsers.add(""); // add empty posting userid - to represent site feed !
                }
                catch(Exception e)
                {
                	Throwable validCause = ExceptionStackUtil.getCause(e, RetryingTransactionHelper.RETRY_EXCEPTIONS);
        			if (validCause != null) {
        				throw (RuntimeException) validCause;
        			}
                    throw new Exception("Failed to get site members: "+e);
                }
                
                // Cache them for future use (across activity posts handled) by this same invocation
                siteConnectedUsers.put(siteId, connectedUsers);
            }
            
            recipients.addAll(connectedUsers);
        }
        
        // Add followers to recipient list
        
        // MT Share
        Pair<String, String> userTenantKey = new Pair<String, String>(postUserId, tenantDomain);
        Set<String> followerUsers = followerConnectedUsers.get(userTenantKey);
        if (followerUsers == null)
        {
            try
            {
                followerUsers = getFollowers(postUserId, tenantDomain);
            }
            catch(Exception e)
            {
            	Throwable validCause = ExceptionStackUtil.getCause(e, RetryingTransactionHelper.RETRY_EXCEPTIONS);
    			if (validCause != null) {
    				throw (RuntimeException) validCause;
    			}
                throw new Exception("Failed to get followers: "+e);
            }
            
            // Cache them for future use (across activity posts handled) by this same invocation
            followerConnectedUsers.put(userTenantKey, followerUsers);
        }
        
        if ((null != siteId) && (siteId.length() > 0))
        {
            for (String followerUser : followerUsers)
            {
                // MNT-13234
                // avoid duplicate activities in activities feed
                boolean caseSensitive = ctx.isUserNamesAreCaseSensitive();
                if ((caseSensitive && recipients.contains(followerUser))
                        || (!caseSensitive && (recipients.contains(followerUser) || recipients.contains(followerUser.toLowerCase()))))
                {
                    continue;
                }
                
                Pair<String, String> userSiteKey = new Pair<String, String>(followerUser, siteId);
                Boolean canRead = canUserReadSite.get(userSiteKey);
                if (canRead == null)
                {
                    // site read permission check (note: only check followers since implied for site members)
                    canRead = canReadSite(ctx, siteId, followerUser, tenantDomain);
                    canUserReadSite.put(userSiteKey, canRead);
                }
                
                if (canRead)
                {
                    recipients.add(followerUser);
                }
            }
        }
        else
        {
            recipients.addAll(followerUsers);
        }
        
        
        // Add the originator to recipients
        recipients.add(postUserId);
        
        return recipients;
    }
    
    /**
     * <p>startTransaction.</p>
     *
     * @throws java.sql.SQLException if any.
     */
    public abstract void startTransaction() throws SQLException;

    /**
     * <p>commitTransaction.</p>
     *
     * @throws java.sql.SQLException if any.
     */
    public abstract void commitTransaction() throws SQLException;

    /**
     * <p>rollbackTransaction.</p>
     *
     * @throws java.sql.SQLException if any.
     */
    public abstract void rollbackTransaction() throws SQLException;

    /**
     * <p>endTransaction.</p>
     *
     * @throws java.sql.SQLException if any.
     */
    public abstract void endTransaction() throws SQLException;

    /**
     * <p>selectPosts.</p>
     *
     * @param selector a {@link org.alfresco.repo.domain.activities.ActivityPostEntity} object
     * @return a {@link java.util.List} object
     * @throws java.sql.SQLException if any.
     */
    public abstract List<ActivityPostEntity> selectPosts(ActivityPostEntity selector) throws SQLException;

    /**
     * <p>selectUserFeedControls.</p>
     *
     * @param userId a {@link java.lang.String} object
     * @return a {@link java.util.List} object
     * @throws java.sql.SQLException if any.
     */
    public abstract List<FeedControlEntity> selectUserFeedControls(String userId) throws SQLException;

    /**
     * <p>insertFeedEntry.</p>
     *
     * @param feed a {@link org.alfresco.repo.domain.activities.ActivityFeedEntity} object
     * @return a long
     * @throws java.sql.SQLException if any.
     */
    public abstract long insertFeedEntry(ActivityFeedEntity feed) throws SQLException;

    /**
     * <p>updatePostStatus.</p>
     *
     * @param id a long
     * @param status a {@link org.alfresco.repo.domain.activities.ActivityPostEntity.STATUS} object
     * @return a int
     * @throws java.sql.SQLException if any.
     */
    public abstract int updatePostStatus(long id, ActivityPostEntity.STATUS status) throws SQLException;

    /**
     * <p>callWebScript.</p>
     *
     * @param urlString a {@link java.lang.String} object
     * @param ticket a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     * @throws java.net.MalformedURLException if any.
     * @throws java.net.URISyntaxException if any.
     * @throws java.io.IOException if any.
     */
    protected String callWebScript(String urlString, String ticket) throws MalformedURLException, URISyntaxException, IOException
    {
        URL url = new URL(urlString);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Request URI: " + url.toURI());
        }
        
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        
        if (ticket != null)
        {
            // add Base64 encoded authorization header
            // refer to: http://wiki.alfresco.com/wiki/Web_Scripts_Framework#HTTP_Basic_Authentication
            conn.addRequestProperty("Authorization", "Basic " + Base64.encodeBytes(ticket.getBytes()));
        }
        
        String result = null;
        InputStream is = null;
        BufferedReader br = null;
        
        try
        {
            is = conn.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            
            String line = null;
            StringBuffer sb = new StringBuffer();
            while(((line = br.readLine()) !=null))  
            {
                sb.append(line);
            }
            
            result = sb.toString();
            
            if (logger.isDebugEnabled())
            {
                int responseCode = conn.getResponseCode();
                logger.debug("Response code: " + responseCode);
            }
        }
        finally
        {
            if (br != null) { br.close(); };
            if (is != null) { is.close(); };
        }
        
        return result;
    }
    
    /**
     * <p>getTenantName.</p>
     *
     * @param name a {@link java.lang.String} object
     * @param tenantDomain a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    protected String getTenantName(String name, String tenantDomain)
    {
        // note: for MT impl, see override in LocalFeedTaskProcessor
        return name;
    }
    
    /**
     * <p>getTenantDomain.</p>
     *
     * @param name a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    protected String getTenantDomain(String name)
    {
        // note: for MT impl, see override in LocalFeedTaskProcessor
        return TenantService.DEFAULT_DOMAIN;
    }
    
    /**
     * <p>getSiteMembers.</p>
     *
     * @param ctx a {@link org.alfresco.repo.activities.feed.RepoCtx} object
     * @param siteId a {@link java.lang.String} object
     * @param tenantDomain a {@link java.lang.String} object
     * @return a {@link java.util.Set} object
     * @throws java.lang.Exception if any.
     */
    protected Set<String> getSiteMembers(RepoCtx ctx, String siteId, String tenantDomain) throws Exception
    {
        // note: tenant domain ignored her - it should already be part of the siteId
        Set<String> members = new HashSet<String>();
        if ((siteId != null) && (siteId.length() != 0))
        {
            StringBuffer sbUrl = new StringBuffer();
            sbUrl.append(ctx.getRepoEndPoint()).
                  append(URL_SERVICE_SITES).append("/").append(siteId).append(URL_MEMBERSHIPS);
            
            String jsonArrayResult = callWebScript(sbUrl.toString(), ctx.getTicket());
            if ((jsonArrayResult != null) && (jsonArrayResult.length() != 0))
            {
                JSONArray ja = new JSONArray(jsonArrayResult);
                for (int i = 0; i < ja.length(); i++)
                {
                    JSONObject member = (JSONObject)ja.get(i);
                    JSONObject person = (JSONObject)member.getJSONObject("person");
                    
                    String userName = person.getString("userName");
                    if (! ctx.isUserNamesAreCaseSensitive())
                    {
                        userName = userName.toLowerCase();
                    }
                    members.add(userName);
                }
            }
        }
        
        return members;
    }
    
    /**
     * <p>getFollowers.</p>
     *
     * @param userId a {@link java.lang.String} object
     * @param tenantDomain a {@link java.lang.String} object
     * @return a {@link java.util.Set} object
     * @throws java.lang.Exception if any.
     */
    protected abstract Set<String> getFollowers(String userId, String tenantDomain) throws Exception;
    /**
     * <p>canReadSite.</p>
     *
     * @param ctx a {@link org.alfresco.repo.activities.feed.RepoCtx} object
     * @param siteIdIn a {@link java.lang.String} object
     * @param connectedUser a {@link java.lang.String} object
     * @param tenantDomain a {@link java.lang.String} object
     * @return a boolean
     * @throws java.lang.Exception if any.
     */
    protected abstract boolean canReadSite(final RepoCtx ctx, String siteIdIn, String connectedUser, final String tenantDomain) throws Exception;
    /**
     * <p>canRead.</p>
     *
     * @param ctx a {@link org.alfresco.repo.activities.feed.RepoCtx} object
     * @param connectedUser a {@link java.lang.String} object
     * @param model a {@link java.util.Map} object
     * @return a boolean
     * @throws java.lang.Exception if any.
     */
    protected abstract boolean canRead(RepoCtx ctx, final String connectedUser, Map<String, Object> model) throws Exception;
    
    /**
     * <p>getActivityTypeTemplates.</p>
     *
     * @param repoEndPoint a {@link java.lang.String} object
     * @param ticket a {@link java.lang.String} object
     * @param subPath a {@link java.lang.String} object
     * @return a {@link java.util.Map} object
     * @throws java.lang.Exception if any.
     */
    protected Map<String, List<String>> getActivityTypeTemplates(String repoEndPoint, String ticket, String subPath) throws Exception
    {
        StringBuffer sbUrl = new StringBuffer();
        sbUrl.append(repoEndPoint).append(URL_SERVICE_TEMPLATES).append(subPath).append("*").append("?format=json");
        
        String jsonArrayResult = null;
        try
        {
            jsonArrayResult = callWebScript(sbUrl.toString(), ticket);
        }
        catch (FileNotFoundException e)
        {
            return null;
        }
        
        List<String> allTemplateNames = new ArrayList<String>(10);
        
        if ((jsonArrayResult != null) && (jsonArrayResult.length() != 0))
        {
            JSONArray ja = new JSONArray(jsonArrayResult);
            for (int i = 0; i < ja.length(); i++)
            {
                String name = ja.getString(i);
                allTemplateNames.add(name);
            }
        }
        
        return getActivityTemplates(allTemplateNames);
    }

    /**
     * <p>getActivityTemplates.</p>
     *
     * @param allTemplateNames a {@link java.util.List} object
     * @return a {@link java.util.Map} object
     */
    protected Map<String, List<String>> getActivityTemplates(List<String> allTemplateNames)
    {
        Map<String, List<String>> activityTemplates = new HashMap<String, List<String>>(10);
        
        for (String template : allTemplateNames)
        {
            if (! template.contains(" (Working Copy)."))
            {
                // assume template path = <path>/<base-activityType>.<format>.ftl
                // and base-activityType can contain "."
                
                String baseActivityType = template;
                int idx1 = baseActivityType.lastIndexOf("/");
                if (idx1 != -1)
                {
                    baseActivityType = baseActivityType.substring(idx1+1);
                }
                
                int idx2 = baseActivityType.lastIndexOf(".");
                if (idx2 != -1)
                {
                    int idx3 = baseActivityType.substring(0, idx2).lastIndexOf(".");
                    if (idx3 != -1)
                    {
                        baseActivityType = baseActivityType.substring(0, idx3);
                        
                        List<String> activityTypeTemplateList = activityTemplates.get(baseActivityType);
                        if (activityTypeTemplateList == null)
                        {
                            activityTypeTemplateList = new ArrayList<String>(1);
                            activityTemplates.put(baseActivityType, activityTypeTemplateList);
                        }
                        activityTypeTemplateList.add(template);
                    }
                }
            }
        }
        
        return activityTemplates;
    }

    /**
     * <p>getFreemarkerConfiguration.</p>
     *
     * @param ctx a {@link org.alfresco.repo.activities.feed.RepoCtx} object
     * @return a {@link freemarker.template.Configuration} object
     */
    protected Configuration getFreemarkerConfiguration(RepoCtx ctx)
    {
        synchronized (freemarkerConfigs)
        {
            Configuration cfg = freemarkerConfigs.get(ctx.getRepoEndPoint());
            if (cfg == null)
            {
                cfg = new Configuration();
                cfg.setObjectWrapper(new DefaultObjectWrapper());

                // custom template loader
                cfg.setTemplateLoader(new TemplateWebScriptLoader(ctx.getRepoEndPoint(), ctx.getTicket()));

                // TODO review i18n
                cfg.setLocalizedLookup(false);
                cfg.setIncompatibleImprovements(new Version(2, 3, 20));
                cfg.setNewBuiltinClassResolver(TemplateClassResolver.SAFER_RESOLVER);
                
                freemarkerConfigs.put(ctx.getRepoEndPoint(), cfg);
            }
            return cfg;
        }
    }

    /**
     * <p>processFreemarker.</p>
     *
     * @param templateCache a {@link java.util.Map} object
     * @param fmTemplate a {@link java.lang.String} object
     * @param cfg a {@link freemarker.template.Configuration} object
     * @param model a {@link java.util.Map} object
     * @return a {@link java.lang.String} object
     * @throws java.io.IOException if any.
     * @throws freemarker.template.TemplateException if any.
     * @throws java.lang.Exception if any.
     */
    protected String processFreemarker(Map<String, Template> templateCache, String fmTemplate, Configuration cfg, Map<String, Object> model) throws IOException, TemplateException, Exception
    {
        // Save on lots of modification date checking by caching templates locally
        Template myTemplate = templateCache.get(fmTemplate);
        if (myTemplate == null)
        {
            myTemplate = cfg.getTemplate(fmTemplate);
            templateCache.put(fmTemplate, myTemplate);
        }
        
        StringWriter textWriter = new StringWriter();
        myTemplate.process(model, textWriter);
        
        return textWriter.toString();
    }

    /**
     * <p>getFeedControls.</p>
     *
     * @param connectedUser a {@link java.lang.String} object
     * @return a {@link java.util.List} object
     * @throws java.sql.SQLException if any.
     */
    protected List<FeedControlEntity> getFeedControls(String connectedUser) throws SQLException
    {
        //MNT-9104 If username contains uppercase letters the action of joining a site will not be displayed in "My activities" 
        if (! userNamesAreCaseSensitive)
        {
            connectedUser = connectedUser.toLowerCase();
        }
        
        return selectUserFeedControls(connectedUser);
    }

    /**
     * <p>acceptActivity.</p>
     *
     * @param activityPost a {@link org.alfresco.repo.domain.activities.ActivityPostEntity} object
     * @param feedControls a {@link java.util.List} object
     * @return a boolean
     */
    protected boolean acceptActivity(ActivityPostEntity activityPost, List<FeedControlEntity> feedControls)
    {
        if (feedControls == null)
        {
            return true;
        }
        
        for (FeedControlEntity feedControl : feedControls)
        {
            if (ActivitiesDAO.KEY_ACTIVITY_NULL_VALUE.equals(feedControl.getSiteNetwork()) && (feedControl.getAppTool() != null))
            {
                if (feedControl.getAppTool().equals(activityPost.getAppTool()))
                {
                    // exclude this appTool (across sites)
                    return false;
                }
            }
            else if (((feedControl.getAppTool() == null) || (feedControl.getAppTool().length() == 0)) && (feedControl.getSiteNetwork() != null))
            {
                if (feedControl.getSiteNetwork().equals(activityPost.getSiteNetwork()))
                {
                    // exclude this site (across appTools)
                    return false;
                }
            }
            else if (((feedControl.getSiteNetwork() != null) && (feedControl.getSiteNetwork().length() > 0)) &&
                     ((feedControl.getAppTool() != null) && (feedControl.getAppTool().length() > 0)))
            {
                if ((feedControl.getSiteNetwork().equals(activityPost.getSiteNetwork())) &&
                    (feedControl.getAppTool().equals(activityPost.getAppTool())))
                {
                    // exclude this appTool for this site
                    return false;
                }
            }
        }
        
        return true;
    }

    /**
     * <p>addMissingFormats.</p>
     *
     * @param activityType a {@link java.lang.String} object
     * @param fmTemplates a {@link java.util.List} object
     * @param templatesToAdd a {@link java.util.List} object
     */
    protected void addMissingFormats(String activityType, List<String> fmTemplates, List<String> templatesToAdd)
    {
        for (String templateToAdd : templatesToAdd)
        {
            int idx1 = templateToAdd.lastIndexOf(".");
            if (idx1 != -1)
            {
                int idx2 = templateToAdd.substring(0, idx1).lastIndexOf(".");
                if (idx2 != -1)
                {
                    String templateFormat = templateToAdd.substring(idx2+1, idx1);
                    
                    boolean found = false;
                    for (String fmTemplate : fmTemplates)
                    {
                        if (fmTemplate.contains("."+templateFormat+"."))
                        {
                            found = true;
                        }
                    }
                    
                    if (! found)
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Add template '" + templateToAdd + "' for type '" + activityType + "'");
                        }
                        fmTemplates.add(templateToAdd);
                    }
                }
            }
        }
    }

    /**
     * <p>getTemplateSubPath.</p>
     *
     * @param activityType a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    protected String getTemplateSubPath(String activityType)
    {
        return (! activityType.startsWith("/") ? "/" : "") + activityType.replace(".", "/");
    }
    
    /**
     * <p>getBaseActivityType.</p>
     *
     * @param activityType a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    protected String getBaseActivityType(String activityType)
    {
        String[] parts = activityType.split("\\.");
        
        return (parts.length != 0 ? parts[parts.length-1] : "");
    }
    
    protected class TemplateWebScriptLoader extends URLTemplateLoader
    {
        private String repoEndPoint;
        private String ticketId;
        
        public TemplateWebScriptLoader(String repoEndPoint, String ticketId)
        {
            this.repoEndPoint = repoEndPoint;
            this.ticketId = ticketId;
            }
        
        public URL getURL(String templatePath)
        {
            try
            {
                StringBuffer sb = new StringBuffer();
                sb.append(this.repoEndPoint).
                   append(URL_SERVICE_TEMPLATE).append("/").append(templatePath).
                   append("?format=text").
                   append("&alf_ticket=").append(ticketId);
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("getURL: " + sb.toString());
                }
                
                return new URL(sb.toString());
            } 
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
         }
    }
}

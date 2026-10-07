/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.repo.action.executer;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.template.DateCompareMethod;
import org.alfresco.repo.template.HasAspectMethod;
import org.alfresco.repo.template.I18NMessageMethod;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.Pair;
import org.alfresco.util.UrlUtil;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.util.StringUtils;

import jakarta.mail.Address;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * Mail action executor implementation.
 * 
 * <em>Note on executing this action as System:</em> it is allowed to execute {@link #NAME mail} actions as system.
 * However there is a limitation if you do so. Because the system user is not a normal user and specifically because
 * there is no corresponding {@link org.alfresco.model.ContentModel#TYPE_PERSON cm:person} node for system, it is not possible to use
 * any reference to that person in the associated email template. Various email templates use a '{@link TemplateNode person}' object
 * in the FTL model to access things like first name, last name etc.
 * In the case of mail actions sent while running as system, none of these will be available.
 *
 * @author Roy Wetherall
 */

/*
 * mrogers
Thinking over MNT-11488 last night I was considering the requirements for a single message for each action and the possibility of a "bulk mail action." that sends many messages. However it occurs to me that we already have this split in the API (Although i couldn't find any documentation which has left the expected functionality confused and the implementation adrift.) So I'm changing my guidance.
There is a need to document (javadoc) the interface so we tie down expected behaviour. And then refactor since the code is confused.
Here's my thinking:
If the to_many parameter is set then it should be a "bulk" email which sends many individual messages.
If the to_many parameter is incompatible with the TO parameter which will be is ignored or will throw an Illegal Argument Exception.
If the to_many parameter is incompatible with the proposed CC parameter which will be is ignored or will throw an Illegal Argument Exception.
If the to_many parameter is incompatible with the proposed BCC parameter which will be is ignored or will throw an Illegal Argument Exception.
If the to_many parameter is not specified then it results in a single message regardless of other settings.
We should probably add CC and BCC parameters which can be specified alonside TO
We should make TO multi-valued
If we allow multiple TO then the single message is only in the locale appropriate to the first TO.
We should allow a list of USER authority name or a email address in TO or TO_MANY.
We should probably also allow GROUP authority names in TO and TO_MANY however for now lets just make sure it works with TO_MANY
Other implications follow through from this big switch approach and affect the implementation.
For example should we allow PARAM_SEND_AFTER_COMMIT for bulk email (since it makes implementation hard.)
Likewise the template handling with locale is clarified. For bulk its an individual message so it has an individual locale.
And with a bulk email we should probably carry on sending even after errors and then have some sort of bulk report of errors.
TEMPLATES can be used with either TO and TO_MANY
*/
public class MailActionExecuter extends ActionExecuterAbstractBase
                                implements InitializingBean, TestModeable
{
    private static Log logger = LogFactory.getLog(MailActionExecuter.class);
    
    /**
     * Action executor constants
     */
    public static final String NAME = "mail";
    /** Constant <code>PARAM_LOCALE="locale"</code> */
    public static final String PARAM_LOCALE = "locale";
    /** Constant <code>PARAM_TO="to"</code> */
    public static final String PARAM_TO = "to";
    /** Constant <code>PARAM_CC="cc"</code> */
    public static final String PARAM_CC = "cc";
    /** Constant <code>PARAM_BCC="bcc"</code> */
    public static final String PARAM_BCC = "bcc";
    /** Constant <code>PARAM_TO_MANY="to_many"</code> */
    public static final String PARAM_TO_MANY = "to_many";
    /** Constant <code>PARAM_SUBJECT="subject"</code> */
    public static final String PARAM_SUBJECT = "subject";
    /** Constant <code>PARAM_SUBJECT_PARAMS="subjectParams"</code> */
    public static final String PARAM_SUBJECT_PARAMS = "subjectParams";
    /** Constant <code>PARAM_TEXT="text"</code> */
    public static final String PARAM_TEXT = "text";
    /** Constant <code>PARAM_HTML="html"</code> */
    public static final String PARAM_HTML = "html";
    /** Constant <code>PARAM_FROM="from"</code> */
    public static final String PARAM_FROM = "from";
    /** Constant <code>PARAM_FROM_PERSONAL_NAME="fromPersonalName"</code> */
    public static final String PARAM_FROM_PERSONAL_NAME = "fromPersonalName";
    /** Constant <code>PARAM_TEMPLATE="template"</code> */
    public static final String PARAM_TEMPLATE = "template";
    /** Constant <code>PARAM_TEMPLATE_MODEL="template_model"</code> */
    public static final String PARAM_TEMPLATE_MODEL = "template_model";
    /** Constant <code>PARAM_IGNORE_SEND_FAILURE="ignore_send_failure"</code> */
    public static final String PARAM_IGNORE_SEND_FAILURE = "ignore_send_failure";
    /** Constant <code>PARAM_SEND_AFTER_COMMIT="send_after_commit"</code> */
    public static final String PARAM_SEND_AFTER_COMMIT = "send_after_commit";
   
    /**
     * From address
     */
    // beCPG
    private static final String FROM_ADDRESS = "no-reply@becpg.fr";

    /** Recipients whose e-mail address starts with this prefix never receive any message. */
    private static final String NO_EMAIL_PREFIX = "no-email";

    private static final List<RecipientType> RECIPIENT_TYPES = List.of(RecipientType.TO, RecipientType.CC, RecipientType.BCC);
    
    private static final String DEFAULT_MAIL_LOGO_URL = "/res/components/images/becpg-footer-logo.png";
    
    /**
     * The java mail sender
     */
    private JavaMailSender mailService;
    
    /**
     * The Template service
     */
    private TemplateService templateService;
    
    /**
     * The Person service
     */
    private PersonService personService;
    
    /**
     * The Authentication service
     */
    private AuthenticationService authService;
    
    /**
     * The Node Service
     */
    private NodeService nodeService;
    
    /**
     * The Authority Service
     */
    private AuthorityService authorityService;
    
    /**
     * The Service registry
     */
    private ServiceRegistry serviceRegistry;
    
    /**
     * System Administration parameters, including URL information
     */
    private SysAdminParams sysAdminParams;
    
    /**
     * The Preference Service
     */
    private PreferenceService preferenceService;
    
    /**
     * The Tenant Service
     */
    private TenantService tenantService;
    
    private AttributeService attributeService;
    
    /**
     * Mail header encoding scheme
     */
    private String headerEncoding = null;
    
    /**
     * Default from address
     */
    private String fromDefaultAddress = null;
    
    /**
     * Is the from field enabled? Or must we always use the default address.
     */
    private boolean fromEnabled = true;
    
    
    private boolean sendTestMessage = false;
    private String testMessageTo = null;
    private String testMessageSubject = "Test message";
    private String testMessageText = "This is a test message.";
    // beCPG
    private String mailLogoUrl = null;

    private boolean validateAddresses = true;
    
    /**
     * Test mode prevents email messages from being sent.
     * It is used when unit testing when we don't actually want to send out email messages.
     * 
     * MER 20/11/2009 This is a quick and dirty fix. It should be replaced by being 
     * "mocked out" or some other better way of running the unit tests. 
     */
    private boolean testMode = false;
    private MimeMessage lastTestMessage;
    private int testSentCount;

    private TemplateImageResolver imageResolver;
    
    /**
     * <p>Setter for the field <code>attributeService</code>.</p>
     *
     * @param attributeService a {@link org.alfresco.service.cmr.attributes.AttributeService} object
     */
    public void setAttributeService(AttributeService attributeService) {
		this.attributeService = attributeService;
	}

    // beCPG
    /**
     * <p>Setter for the field <code>mailLogoUrl</code>.</p>
     *
     * @param mailLogoUrl a {@link java.lang.String} object
     */
    public void setMailLogoUrl(String mailLogoUrl) {
		this.mailLogoUrl = mailLogoUrl;
	}
    
    /**
     * <p>Setter for the field <code>mailService</code>.</p>
     *
     * @param javaMailSender    the java mail sender
     */
    public void setMailService(JavaMailSender javaMailSender) 
    {
        this.mailService = javaMailSender;
    }
    
    /**
     * <p>Setter for the field <code>templateService</code>.</p>
     *
     * @param templateService   the TemplateService
     */
    public void setTemplateService(TemplateService templateService)
    {
        this.templateService = templateService;
    }
    
    /**
     * <p>Setter for the field <code>personService</code>.</p>
     *
     * @param personService     the PersonService
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    /**
     * <p>Setter for the field <code>preferenceService</code>.</p>
     *
     * @param preferenceService a {@link org.alfresco.service.cmr.preference.PreferenceService} object
     */
    public void setPreferenceService(PreferenceService preferenceService)
    {
        this.preferenceService = preferenceService;
    }
    
    /**
     * <p>setAuthenticationService.</p>
     *
     * @param authService       the AuthenticationService
     */
    public void setAuthenticationService(AuthenticationService authService)
    {
        this.authService = authService;
    }
    
    /**
     * <p>Setter for the field <code>serviceRegistry</code>.</p>
     *
     * @param serviceRegistry   the ServiceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }
    
    /**
     * <p>Setter for the field <code>authorityService</code>.</p>
     *
     * @param authorityService  the AuthorityService
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    /**
     * <p>Setter for the field <code>nodeService</code>.</p>
     *
     * @param nodeService       the NodeService to set.
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * <p>Setter for the field <code>tenantService</code>.</p>
     *
     * @param tenantService       the TenantService to set.
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    /**
     * <p>Setter for the field <code>headerEncoding</code>.</p>
     *
     * @param headerEncoding     The mail header encoding to set.
     */
    public void setHeaderEncoding(String headerEncoding)
    {
        this.headerEncoding = headerEncoding;
    }
    
    /**
     * <p>setFromAddress.</p>
     *
     * @param fromAddress   The default mail address.
     */
    public void setFromAddress(String fromAddress)
    {
        this.fromDefaultAddress = fromAddress;
    }

    /**
     * <p>Setter for the field <code>sysAdminParams</code>.</p>
     *
     * @param sysAdminParams a {@link org.alfresco.repo.admin.SysAdminParams} object
     */
    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }
    
    /**
     * <p>Setter for the field <code>imageResolver</code>.</p>
     *
     * @param imageResolver a {@link org.alfresco.service.cmr.repository.TemplateImageResolver} object
     */
    public void setImageResolver(TemplateImageResolver imageResolver)
    {
        this.imageResolver = imageResolver;
    }
    
    /**
     * <p>Setter for the field <code>testMessageTo</code>.</p>
     *
     * @param testMessageTo a {@link java.lang.String} object
     */
    public void setTestMessageTo(String testMessageTo)
    {
        this.testMessageTo = testMessageTo;
    }
    
    /**
     * <p>Getter for the field <code>testMessageTo</code>.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getTestMessageTo()
    {
        return testMessageTo;
    }
    
    /**
     * <p>Setter for the field <code>testMessageSubject</code>.</p>
     *
     * @param testMessageSubject a {@link java.lang.String} object
     */
    public void setTestMessageSubject(String testMessageSubject)
    {
        this.testMessageSubject = testMessageSubject;
    }
    
    /**
     * <p>Setter for the field <code>testMessageText</code>.</p>
     *
     * @param testMessageText a {@link java.lang.String} object
     */
    public void setTestMessageText(String testMessageText)
    {
        this.testMessageText = testMessageText;
    }

    /**
     * <p>Setter for the field <code>sendTestMessage</code>.</p>
     *
     * @param sendTestMessage a boolean
     */
    public void setSendTestMessage(boolean sendTestMessage)
    {
        this.sendTestMessage = sendTestMessage;
    }
    
    /**
     * This stores an email address which, if it is set, overrides ALL email recipients sent from
     * this class. It is intended for dev/test usage only !!
     */
    private String testModeRecipient;

    /**
     * Send a test message
     *
     * @return true, message sent
     */
    public boolean sendTestMessage() 
    {
        if(testMessageTo == null || testMessageTo.length() == 0)
        {
            throw new AlfrescoRuntimeException("email.outbound.err.test.no.to");
        }
        if(testMessageSubject == null || testMessageSubject.length() == 0)
        {
            throw new AlfrescoRuntimeException("email.outbound.err.test.no.subject");
        }
        if(testMessageText == null || testMessageText.length() == 0)
        {
            throw new AlfrescoRuntimeException("email.outbound.err.test.no.text");
        }
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put(PARAM_TO, testMessageTo);
        params.put(PARAM_SUBJECT, testMessageSubject);
        params.put(PARAM_TEXT, testMessageText);
        
        Action ruleAction = serviceRegistry.getActionService().createAction(NAME, params);
        
        MimeMessageHelper message = prepareEmail(ruleAction, null,
                new Pair<String, Locale>(testMessageTo, getLocaleForUser(testMessageTo)), getFrom(ruleAction));
        try
        {
            mailService.send(message.getMimeMessage());
            onSend();
        }
        catch (MailException me)
        {
            onFail();
            StringBuffer txt = new StringBuffer();
            
            txt.append(me.getClass().getName() + ", " + me.getMessage());
            
            Throwable cause = me.getCause();
            while (cause != null)
            {
                txt.append(", ");
                txt.append(cause.getClass().getName() + ", " + cause.getMessage());
                cause = cause.getCause();
            }
            
            Object[] args = {testMessageTo, txt.toString()};
            throw new AlfrescoRuntimeException("email.outbound.err.send.failed", args, me);
        }
        
        return true;
    }
    
    /**
     * <p>Setter for the field <code>testModeRecipient</code>.</p>
     *
     * @param testModeRecipient a {@link java.lang.String} object
     */
    public void setTestModeRecipient(String testModeRecipient)
    {
        this.testModeRecipient = testModeRecipient;
    }

    /**
     * <p>Setter for the field <code>validateAddresses</code>.</p>
     *
     * @param validateAddresses a boolean
     */
    public void setValidateAddresses(boolean validateAddresses)
    {
        this.validateAddresses = validateAddresses;
    }

    
    /** {@inheritDoc} */
    @Override
    public void init()
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("Init called, testMessageTo=" + testMessageTo);
        }
        
        numberSuccessfulSends.set(0);
        numberFailedSends.set(0);
        
        super.init();
        if (sendTestMessage && testMessageTo != null)
        {
            AuthenticationUtil.runAs(new RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    Map<String, Serializable> params = new HashMap<String, Serializable>();
                    params.put(PARAM_TO, testMessageTo);
                    params.put(PARAM_SUBJECT, testMessageSubject);
                    params.put(PARAM_TEXT, testMessageText);

                    Action ruleAction = serviceRegistry.getActionService().createAction(NAME, params);
                    executeImpl(ruleAction, null);
                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());
        }
    }

    /**
     * Initialise bean
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception
    {
        if (fromDefaultAddress == null || fromDefaultAddress.length() == 0)
        {
            fromDefaultAddress = FROM_ADDRESS;
        }
        
    }
    
    /**
     * {@inheritDoc}
     *
     * Send an email message
     */
    @Override
    protected void executeImpl(
            final Action ruleAction,
            final NodeRef actionedUponNodeRef) 
    {

        //Prepare our messages before the commit, in-case of deletes
        MimeMessageHelper[] messages = null; 
        if (validNodeRefIfPresent(actionedUponNodeRef))
        {
            messages = prepareEmails(ruleAction, actionedUponNodeRef);
        }
        final MimeMessageHelper[] finalMessages = messages;
        
        //Send out messages
        if(finalMessages!=null){
            if (sendAfterCommit(ruleAction))
            {
                AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter()
                {
                    @Override
                    public void afterCommit()
                    {
                        RetryingTransactionHelper helper = serviceRegistry.getTransactionService().getRetryingTransactionHelper();
                        helper.doInTransaction(new RetryingTransactionCallback<Void>()
                        {
                            @Override
                            public Void execute() throws Throwable
                            {
                                for (MimeMessageHelper message : finalMessages) {
                                    sendEmail(ruleAction, message);
                                }
                                
                                return null;
                            }
                        }, false, true);
                    }
                });
            }
            else 
            {
                    for (MimeMessageHelper message : finalMessages) {
                    if (message != null)
                    {
                        sendEmail(ruleAction, message);
                    }
                    }
            }
        }

    }
    
    
    private boolean validNodeRefIfPresent(NodeRef actionedUponNodeRef)
    {
        if (actionedUponNodeRef == null)
        {
            // We must expect that null might be passed in (ALF-11625)
            // since the mail action might not relate to a specific nodeRef.
            return true;
        }
        else
        {
            // Only try and send the email if the actioned upon node reference still exists
            // (i.e. if one has been specified it must be valid)
            return nodeService.exists(actionedUponNodeRef);
        }
    }
    
    private boolean sendAfterCommit(Action action)
    {
        Boolean sendAfterCommit = (Boolean) action.getParameterValue(PARAM_SEND_AFTER_COMMIT);
        return sendAfterCommit == null ? false : sendAfterCommit.booleanValue();
    }
    
    private MimeMessageHelper[] prepareEmails(final Action ruleAction, final NodeRef actionedUponNodeRef)
    {
        Serializable ref = ruleAction.getParameterValue(PARAM_TEMPLATE);
        String templateRef = (ref instanceof NodeRef ? ((NodeRef)ref).toString() : (String)ref);
        if (templateRef == null)
        {
            // send as bulk message if there is no template
            MimeMessageHelper[] messages = new MimeMessageHelper[1];
            messages[0] = prepareEmail(ruleAction, actionedUponNodeRef, null, null);
            return messages;
        }
        
        Collection<Pair<String, Locale>> recipients = getRecipients(ruleAction);
        
        Iterator<Pair<String, Locale>> it = recipients.iterator();
        
        while (it.hasNext()) {
        	Pair<String, Locale> recipient = it.next();
        	if (isOptedOutAddress(recipient.getFirst())) {
        		it.remove();
        		logger.debug("Remove opted out address from recipients");
        	}
        }
        
        Pair<InternetAddress, Locale> from = getFrom(ruleAction);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("From: address=" + from.getFirst() + " ,locale=" + from.getSecond());
        }
        
        MimeMessageHelper[] messages = new MimeMessageHelper[recipients.size()];
        int recipientIndex = 0;
        for (Pair<String, Locale> recipient : recipients)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Recipient: address=" + recipient.getFirst() + " ,locale=" + recipient.getSecond());
            }
            
            messages[recipientIndex] = prepareEmail(ruleAction, actionedUponNodeRef, recipient, from);
            recipientIndex++;
        }
        return messages;
    }
    
    /**
     * <p>prepareEmail.</p>
     *
     * @param ruleAction a {@link org.alfresco.service.cmr.action.Action} object
     * @param actionedUponNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
     * @param recipient a {@link org.alfresco.util.Pair} object
     * @param sender a {@link org.alfresco.util.Pair} object
     * @return a {@link org.springframework.mail.javamail.MimeMessageHelper} object
     */
    public MimeMessageHelper prepareEmail(final Action ruleAction , final NodeRef actionedUponNodeRef, final Pair<String, Locale> recipient, final Pair<InternetAddress, Locale> sender)
    {
        // Create the mime mail message.
        // Hack: using an array here to get around the fact that inner classes aren't closures.
        // The MimeMessagePreparator.prepare() signature does not allow us to return a value and yet
        // we can't set a result on a bare, non-final object reference due to Java language restrictions.
        final MimeMessageHelper[] messageRef = new MimeMessageHelper[1];
        MimeMessagePreparator mailPreparer = new MimeMessagePreparator()
        {
            @SuppressWarnings("unchecked")
            public void prepare(MimeMessage mimeMessage) throws MessagingException
            {
                if (logger.isDebugEnabled())
                {
                   logger.debug(ruleAction.getParameterValues());
                }
                
                messageRef[0] = new MimeMessageHelper(mimeMessage);
                
                // set header encoding if one has been supplied
                if (headerEncoding != null && headerEncoding.length() != 0)
                {
                    mimeMessage.setHeader("Content-Transfer-Encoding", headerEncoding);
                }
                
                // set recipient
                String to = (String)ruleAction.getParameterValue(PARAM_TO);
                String toRecipients = null;
                if (to != null && to.length() != 0)
                {
                    messageRef[0].setTo(to);
                    toRecipients = to;

                    // Note: there is no validation on the username to check that it actually is an email address.
                    // TODO Fix this.

                    Serializable ccValue = (Serializable)ruleAction.getParameterValue(PARAM_CC);
                    if(ccValue != null)
                    {
                        if (ccValue instanceof String)
                        {
                            String cc = (String)ccValue;
                            if(cc.length() > 0)
                            {
                                messageRef[0].setCc(cc);
                            }

                        }
                        else if (ccValue instanceof List<?>)
                        {
                            List<String>s = (List<String>)ccValue;
                            messageRef[0].setCc(s.toArray(new String[s.size()]));
                        }
                        else if (ccValue.getClass().isArray())
                        {
                            messageRef[0].setCc((String[])ccValue);
                        }
                        
                    }
                    Serializable bccValue = (Serializable)ruleAction.getParameterValue(PARAM_BCC);
                    if(bccValue != null)
                    {
                        if (bccValue instanceof String)
                        {
                            String bcc = (String)bccValue;
                            if(bcc.length() > 0)
                            {
                                messageRef[0].setBcc(bcc);
                            }

                        }
                        else if (bccValue instanceof List<?>)
                        {
                            List<String>s = (List<String>)bccValue;
                            messageRef[0].setBcc(s.toArray(new String[s.size()]));
                        }
                        else if (bccValue.getClass().isArray())
                        {
                            messageRef[0].setBcc((String[])bccValue);
                        }
                    }
                    
                }
                else
                {
                    // see if multiple recipients have been supplied - as a list of authorities
                    Serializable authoritiesValue = ruleAction.getParameterValue(PARAM_TO_MANY);
                    List<String> authorities = null;
                    if (authoritiesValue != null)
                    {
                        if (authoritiesValue instanceof String)
                        {
                            authorities = new ArrayList<String>(1);
                            authorities.add((String)authoritiesValue);
                        }
                        else
                        {
                            authorities = (List<String>)authoritiesValue;
                        }
                    }
                    
                    if (authorities != null && authorities.size() != 0)
                    {
                        List<String> recipients = new ArrayList<String>(authorities.size());
                        
                        if (logger.isTraceEnabled()) { logger.trace(authorities.size() + " recipient(s) for mail"); }
                        
                        for (String authority : authorities)
                        {
                            final AuthorityType authType = AuthorityType.getAuthorityType(authority);
                            
                            if (logger.isTraceEnabled()) { logger.trace(" authority type: " + authType); }
                            
                            if (authType.equals(AuthorityType.USER))
                            {
                                if (personService.personExists(authority) == true)
                                {
                                    NodeRef person = personService.getPerson(authority);
                                    
                                    if (!personService.isEnabled(authority) && !nodeService.hasAspect(person, ContentModel.ASPECT_ANULLABLE))
                                    {
                                        continue;
                                    }
                                    
                                    String address = (String)nodeService.getProperty(person, ContentModel.PROP_EMAIL);
                                    if (address != null && address.length() != 0 && validateAddress(address))
                                    {
                                        if (logger.isTraceEnabled()) { logger.trace("Recipient (person) exists in Alfresco with known email."); }
                                        recipients.add(address);
                                    }
                                    else
                                    {
                                        if (logger.isTraceEnabled()) { logger.trace("Recipient (person) exists in Alfresco without known email."); }
                                        // If the username looks like an email address, we'll use that.
                                        if (validateAddress(authority)) { recipients.add(authority); }
                                    }
                                }
                                else
                                {
                                    if (logger.isTraceEnabled()) { logger.trace("Recipient does not exist in Alfresco."); }
                                    if (validateAddress(authority)) { recipients.add(authority); }
                                }
                            }
                            else if (authType.equals(AuthorityType.GROUP) || authType.equals(AuthorityType.EVERYONE))
                            {
                                if (logger.isTraceEnabled()) { logger.trace("Recipient is a group..."); }
                                // Notify all members of the group
                                Set<String> users;
                                if (authType.equals(AuthorityType.GROUP))
                                {        
                                    users = authorityService.getContainedAuthorities(AuthorityType.USER, authority, false);
                                }
                                else
                                {
                                    users = authorityService.getAllAuthorities(AuthorityType.USER);
                                }
                                
                                for (String userAuth : users)
                                {
                                    if (personService.personExists(userAuth) == true)
                                    {
                                        if (!personService.isEnabled(userAuth))
                                        {
                                            continue;
                                        }
                                        NodeRef person = personService.getPerson(userAuth);
                                        String address = (String)nodeService.getProperty(person, ContentModel.PROP_EMAIL);
                                        if (address != null && address.length() != 0)
                                        {
                                            recipients.add(address);
                                            if (logger.isTraceEnabled()) { logger.trace("   Group member email is known."); }
                                        }
                                        else
                                        {
                                            if (logger.isTraceEnabled()) { logger.trace("   Group member email not known."); }
                                            if (validateAddress(authority)) { recipients.add(userAuth); }
                                        }
                                    }
                                    else
                                    {
                                        if (logger.isTraceEnabled()) { logger.trace("   Group member person not found"); }
                                        if (validateAddress(authority)) { recipients.add(userAuth); }
                                    }
                                }
                            }
                        }
                        
                        if (logger.isTraceEnabled()) { logger.trace(recipients.size() + " valid recipient(s)."); }
                        
                        if(recipients.size() > 0)
                        {
                            messageRef[0].setTo(recipients.toArray(new String[recipients.size()]));
                            toRecipients = String.join(",", recipients);
                        }
                        else
                        {
                            // All recipients were invalid
                            throw new MailPreparationException(
                                    "All recipients for the mail action were invalid"
                            );
                        }
                    }
                    else
                    {
                        // No recipients have been specified
                        throw new MailPreparationException(
                                "No recipient has been specified for the mail action"
                        );
                    }
                }
                
                // from person - not to be performed for the "admin" or "system" users
                NodeRef fromPerson = null;
                
                final String currentUserName = authService.getCurrentUserName();
                
                final List<String> usersNotToBeUsedInFromField = Arrays.asList(new String[] {AuthenticationUtil.getSystemUserName(),
                                                                                             AuthenticationUtil.getGuestUserName()});
                if ( !usersNotToBeUsedInFromField.contains(currentUserName))
                {
                    fromPerson = personService.getPerson(currentUserName);
                }
                
                if(isFromEnabled())
                {   
                    // Use the FROM parameter in preference to calculating values.
                    String from = (String)ruleAction.getParameterValue(PARAM_FROM);
                    if (from != null && from.length() > 0)
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("from specified as a parameter, from:" + from);
                        }
                        
                        // Check whether or not to use a personal name for the email (will be RFC 2047 encoded)
                        String fromPersonalName = (String)ruleAction.getParameterValue(PARAM_FROM_PERSONAL_NAME);
                        if(fromPersonalName != null && fromPersonalName.length() > 0) 
                        {
                            try
                            {
                                messageRef[0].setFrom(from, fromPersonalName);
                            }
                            catch (UnsupportedEncodingException error)
                            {
                                // Uses the JVM's default encoding, can never be unsupported. Just in case, revert to simple email
                                messageRef[0].setFrom(from);
                            }
                        }
                        else
                        {
                            messageRef[0].setFrom(from);
                        }
                    }
                    else
                    {
                        // set the from address from the current user
                        String fromActualUser = null;
                        if (fromPerson != null)
                        {
                            fromActualUser = (String) nodeService.getProperty(fromPerson, ContentModel.PROP_EMAIL);
                        }
                    
                        if (fromActualUser != null && fromActualUser.length() != 0)
                        {
                            if(logger.isDebugEnabled())
                            {
                                logger.debug("looked up email address for :" + fromPerson + " email from " + fromActualUser);
                            }
                            messageRef[0].setFrom(fromActualUser);
                        }
                        else
                        {
                            // from system or user does not have email address
                            messageRef[0].setFrom(fromDefaultAddress);
                        }
                    }

                }
                else
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("from not enabled - sending from default address:" + fromDefaultAddress);
                    }
                    // from is not enabled.
                    messageRef[0].setFrom(fromDefaultAddress);
                }
                
                /**
                 * 
                 * beCPG Patch always use reply-to and return-path instead of from, 
                 * 	and set from as server default
                 * 
                 */
                
                String from = messageRef[0].getMimeMessage().getFrom()[0].toString();
                if(!fromDefaultAddress.equals(from)) {
                	//Set reply-to
                	messageRef[0].setReplyTo(from);
                	// Set return-path
                	messageRef[0].getMimeMessage().setHeader("mail.smtp.from", from);
                	// override from
                	messageRef[0].setFrom(fromDefaultAddress);
                }
            	
                if (fromPerson != null) {
                	String fromPersonFullName = (String) nodeService.getProperty(fromPerson, ContentModel.PROP_FIRSTNAME) + " " + (String) nodeService.getProperty(fromPerson, ContentModel.PROP_LASTNAME);
                	
                	Serializable templateModelObject = ruleAction.getParameterValues().get(PARAM_TEMPLATE_MODEL);
                	
                	if (templateModelObject instanceof Map) {
                		
                		Map<String, Serializable> templateModel = (Map<String, Serializable>) templateModelObject;
                		
                		Map<String, Serializable> args = (Map<String, Serializable>) templateModel.get("args");
                		
                		args.put("fromPersonFullName", fromPersonFullName);
                	}
                }

                
                // set subject line
                messageRef[0].setSubject((String)ruleAction.getParameterValue(PARAM_SUBJECT));
                
                if ((testModeRecipient != null) && (testModeRecipient.length() > 0) && (! testModeRecipient.equals("${dev.email.recipient.address}")))
                {
                    // If we have an override for the email recipient, we'll send the email to that address instead.
                    // We'll prefix the subject with the original recipient, but leave the email message unchanged in every other way.
                    messageRef[0].setTo(testModeRecipient);
                    
                    String emailRecipient = (String)ruleAction.getParameterValue(PARAM_TO);
                    if (emailRecipient == null)
                    {
                       Object obj = ruleAction.getParameterValue(PARAM_TO_MANY);
                       if (obj != null)
                       {
                           emailRecipient = obj.toString();
                       }
                    }
                    
                    String recipientPrefixedSubject = "(" + emailRecipient + ") " + (String)ruleAction.getParameterValue(PARAM_SUBJECT);
                    
                    messageRef[0].setSubject(recipientPrefixedSubject);
                }
                
                
                // See if an email template has been specified
                String text = null;
                
                // templateRef: either a nodeRef or classpath (see ClasspathRepoTemplateLoader)
                Serializable ref = ruleAction.getParameterValue(PARAM_TEMPLATE);
                String templateRef = (ref instanceof NodeRef ? ((NodeRef)ref).toString() : (String)ref);
                if (templateRef != null)
                {
                    Map<String, Object> suppliedModel = null;
                    if(ruleAction.getParameterValue(PARAM_TEMPLATE_MODEL) != null)
                    {
                        Object m = ruleAction.getParameterValue(PARAM_TEMPLATE_MODEL);
                        if(m instanceof Map)
                        {
                            suppliedModel = (Map<String, Object>)m;
                        }
                        else
                        {
                            logger.warn("Skipping unsupported email template model parameters of type "
                                    + m.getClass().getName() + " : " + m.toString());
                        }
                    }
                    
                    // build the email template model
                    Map<String, Object> model = createEmailTemplateModel(actionedUponNodeRef, suppliedModel, fromPerson, toRecipients);

                    // Determine the locale to use to send the email.
                    Locale locale = recipient.getSecond();
                    if (locale == null)
                    {
                        locale = (Locale)ruleAction.getParameterValue(PARAM_LOCALE);
                    }
                    if (locale == null)
                    {
                        locale = sender.getSecond();
                    }
                    
                    // set subject line
                    String subject = (String)ruleAction.getParameterValue(PARAM_SUBJECT);
                    Object subjectParamsObject = ruleAction.getParameterValue(PARAM_SUBJECT_PARAMS);
                    Object[] subjectParams = null;
                    //Javasctipt pass SubjectParams as ArrayList. see MNT-12534 
                    if (subjectParamsObject instanceof List)
                    {
                        subjectParams = ((List<Object>)subjectParamsObject).toArray();
                    }
                    else if (subjectParamsObject instanceof Object[])
                    {
                        subjectParams = (Object[])subjectParamsObject;
                    }
                    else
                    {
                        if (subjectParamsObject != null)
                        {
                            subjectParams = new Object[]{subjectParamsObject.toString()};
                        }
                    }
                    String localizedSubject = getLocalizedSubject(subject, subjectParams, locale);
                    if (locale == null)
                    {
                        // process the template against the model
                        text = templateService.processTemplate("freemarker", templateRef, model);
                    }
                    else
                    {
                        // process the template against the model
                        text = templateService.processTemplate("freemarker", templateRef, model, locale);
                    }
                    if ((testModeRecipient != null) && (testModeRecipient.length() > 0) && (! testModeRecipient.equals("${dev.email.recipient.address}")))
                    {
                        // If we have an override for the email recipient, we'll send the email to that address instead.
                        // We'll prefix the subject with the original recipient, but leave the email message unchanged in every other way.
                        messageRef[0].setTo(testModeRecipient);
                        
                        String emailRecipient = recipient.getFirst();
                        
                        String recipientPrefixedSubject = "(" + emailRecipient + ") " + localizedSubject;
                        
                        messageRef[0].setSubject(recipientPrefixedSubject);
                    }
                    else 
                    {
                        messageRef[0].setTo(recipient.getFirst());
                        messageRef[0].setSubject(localizedSubject);
                    }
                }
                
                // set the text body of the message
                
                boolean isHTML = false;
                if (text == null)
                {
                    text = (String)ruleAction.getParameterValue(PARAM_TEXT);
                }
                
                if (text != null)
                {
                    if (isHTML(text))
                    {
                        isHTML = true;
                    }
                }
                else
                {
                    text = (String)ruleAction.getParameterValue(PARAM_HTML);
                    if (text != null)
                    {
                        // assume HTML
                        isHTML = true;
                    }
                }
                
                if (text != null)
                {
                    messageRef[0].setText(text, isHTML);
                }
                
            }
        };
        MimeMessage mimeMessage = mailService.createMimeMessage(); 
        try
        {
            mailPreparer.prepare(mimeMessage);
        } catch (Exception e)
        {
            // We're forced to catch java.lang.Exception here. Urgh.
            if (logger.isWarnEnabled())
            {
                logger.warn("Unable to prepare mail message. Skipping.", e);
            }
            return null;
        }
        
        return messageRef[0];
    }
    
    private void sendEmail(final Action ruleAction, MimeMessageHelper preparedMessage)
    {

        try
        {
            if (!stripOptedOutRecipients(preparedMessage.getMimeMessage()))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Skip message, every recipient opted out of e-mails");
                }
                return;
            }

            // Send the message unless we are in "testMode"
            if (!testMode)
            {	
            	mailService.send(preparedMessage.getMimeMessage());
                onSend();
            }
            else
            {
                lastTestMessage = preparedMessage.getMimeMessage();
                testSentCount++;
            }
        }
        catch (NullPointerException | MailException | MessagingException e)
        {
            onFail();
            String to = (String)ruleAction.getParameterValue(PARAM_TO);
            if (to == null)
            {
               Object obj = ruleAction.getParameterValue(PARAM_TO_MANY);
               if (obj != null)
               {
                  to = obj.toString();
               }
            }
            
            // always log the failure
            logger.error("Failed to send email to " + to + " : " + e);
            
            // optionally ignore the throwing of the exception
            Boolean ignoreError = (Boolean)ruleAction.getParameterValue(PARAM_IGNORE_SEND_FAILURE);
            if (ignoreError == null || ignoreError.booleanValue() == false)
            {
                throw new AlfrescoRuntimeException("Failed to send email to:" + to);
            }   
        }
    }
    
    /**
     * Removes from the prepared message every recipient that opted out of e-mails, whichever path built it.
     * A message left without any recipient by an unrelated cause is not abandoned here, so that the mail
     * service keeps reporting it as a failure.
     *
     * @param mimeMessage the message about to be sent
     * @return false only when every recipient of the message opted out of e-mails
     * @throws jakarta.mail.MessagingException if the recipients cannot be read or rewritten
     */
    private boolean stripOptedOutRecipients(MimeMessage mimeMessage) throws MessagingException
    {
        boolean optedOutRemoved = false;
        boolean hasRecipient = false;

        for (RecipientType recipientType : RECIPIENT_TYPES)
        {
            Address[] addresses = mimeMessage.getRecipients(recipientType);
            if (addresses == null)
            {
                continue;
            }

            List<Address> allowedAddresses = allowedRecipients(addresses, recipientType);
            if (allowedAddresses.size() != addresses.length)
            {
                optedOutRemoved = true;
                mimeMessage.setRecipients(recipientType,
                        allowedAddresses.isEmpty() ? null : allowedAddresses.toArray(new Address[allowedAddresses.size()]));
            }
            hasRecipient = hasRecipient || !allowedAddresses.isEmpty();
        }

        return hasRecipient || !optedOutRemoved;
    }

    /**
     * Selects, among the recipients of a single recipient type, those still allowed to receive the message.
     *
     * @param addresses the recipients of that type
     * @param recipientType the recipient type being filtered, for logging purposes
     * @return the recipients that did not opt out of e-mails
     */
    private List<Address> allowedRecipients(Address[] addresses, RecipientType recipientType)
    {
        List<Address> allowedAddresses = new ArrayList<Address>(addresses.length);
        for (Address address : addresses)
        {
            if (isOptedOutAddress(extractAddress(address)))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Remove opted out address from " + recipientType + " recipients");
                }
            }
            else
            {
                allowedAddresses.add(address);
            }
        }
        return allowedAddresses;
    }

    /**
     * Extracts the bare e-mail address, dropping any personal name.
     *
     * @param address the recipient address
     * @return the bare e-mail address
     */
    private static String extractAddress(Address address)
    {
        if (address instanceof InternetAddress internetAddress)
        {
            return internetAddress.getAddress();
        }
        return address.toString();
    }

    /**
     * Tells whether an e-mail address opted out of every message sent by the application, either because it
     * is the unattended sender address or because it carries the opt out prefix.
     *
     * @param address the e-mail address to check
     * @return true if no message must ever be sent to that address
     */
    private static boolean isOptedOutAddress(String address)
    {
        if (address == null)
        {
            return false;
        }

        String normalizedAddress = address.toLowerCase(Locale.ROOT);
        return normalizedAddress.equals(FROM_ADDRESS) || normalizedAddress.startsWith(NO_EMAIL_PREFIX);
    }

    /**
     * Attempt to localize the subject, using the subject parameter as the message key.
     * 
     * @param subject Message key for subject lookup
     * @param params Parameters for the message
     * @param locale Locale to use
     * @return The localized message, or subject if the message format could not be found
     */
    private String getLocalizedSubject(String subject, Object[] params, Locale locale)
    {
        String localizedSubject = null;
        if (locale == null)
        {
            localizedSubject = I18NUtil.getMessage(subject, params);
        }
        else 
        {
            localizedSubject = I18NUtil.getMessage(subject, locale, params);
        }
        
        if (localizedSubject == null)
        {
            return subject;
        }
        else
        {
            return localizedSubject;
        }
        
    }
    
    /**
     * 
     * @param ruleAction Action
     * @return Pair
     */
    private Pair<InternetAddress, Locale> getFrom(Action ruleAction)
    {
        try 
        {
            InternetAddress address;
            Locale locale = null;
            // from person
            String fromPersonName = null;
            
            if (! authService.isCurrentUserTheSystemUser())
            {
                String currentUserName = authService.getCurrentUserName();
                if (currentUserName != null && personExists(currentUserName))
                {
                    fromPersonName = currentUserName;
                    locale = getLocaleForUser(fromPersonName);
                }
            }
                    
            if(isFromEnabled())
            {   
                // Use the FROM parameter in preference to calculating values.
                String from = (String)ruleAction.getParameterValue(PARAM_FROM);
                if (from != null && from.length() > 0)
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("from specified as a parameter, from:" + from);
                    }
                
                    // Check whether or not to use a personal name for the email (will be RFC 2047 encoded)
                    String fromPersonalName = (String)ruleAction.getParameterValue(PARAM_FROM_PERSONAL_NAME);
                    if(fromPersonalName != null && fromPersonalName.length() > 0) 
                    {
                        try
                        {
                            address = new InternetAddress(from, fromPersonalName);
                        }
                        catch (UnsupportedEncodingException error)
                        {
                            address = new InternetAddress(from);
                        }
                    }
                    else
                    {
                        address = new InternetAddress(from);
                    }
                    if (locale == null)
                    {
                        if (personExists(from))
                        {
                            locale = getLocaleForUser(from);
                        }
                    }
                }
                else
                {
                    // FROM enabled but not not specified                
                    String fromActualUser = fromPersonName;
                    if (fromPersonName != null)
                    {
                         fromActualUser = getPersonEmail(fromPersonName);
                    }
              
                    if (fromActualUser != null && fromActualUser.length() != 0)
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("looked up email address for :" + fromPersonName + " email from " + fromActualUser);
                        }
                        address = new InternetAddress(fromActualUser);
                    }
                    else
                    {
                        // from system or user does not have email address
                        address = new InternetAddress(fromDefaultAddress);
                    }
                }
            }
            else
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("from not enabled - sending from default address:" + fromDefaultAddress);
                }
                // from is not enabled.
                address = new InternetAddress(fromDefaultAddress);
            }
            
            return new Pair<InternetAddress, Locale>(address, locale);
        }
        catch (MessagingException ex)
        {
            throw new AlfrescoRuntimeException("Failed to resolve sender mail address");
        }
    }
    
    @SuppressWarnings("unchecked")
    private Collection<Pair<String, Locale>> getRecipients(Action ruleAction) 
    {
        Map<String, Pair<String, Locale>> recipients = new HashMap<String, Pair<String,Locale>>();
        
        // set recipient
        String to = (String)ruleAction.getParameterValue(PARAM_TO);
        if (to != null && to.length() != 0)
        {
            Locale locale = null;
            if (personExists(to))
            {
                locale = getLocaleForUser(to);
            }
            recipients.put(to, new Pair<String, Locale>(to, locale));
        }
        else
        {
            // see if multiple recipients have been supplied - as a list of authorities
            Serializable authoritiesValue = ruleAction.getParameterValue(PARAM_TO_MANY);
            List<String> authorities = null;
            if (authoritiesValue != null)
            {
                if (authoritiesValue instanceof String)
                {
                    authorities = new ArrayList<String>(1);
                    authorities.add((String)authoritiesValue);
                }
                else
                {
                    authorities = (List<String>)authoritiesValue;
                }
            }
            
            if (authorities != null && authorities.size() != 0)
            {
                for (String authority : authorities)
                {
                    AuthorityType authType = AuthorityType.getAuthorityType(authority);
                    if (authType.equals(AuthorityType.USER))
                    {
                        // Formerly, this code checked personExists(auth) but we now support emailing addresses who are not yet Alfresco users.
                        // Check the user name to be a valid email and we don't need to log an error in this case
                        // ALF-19231
                        // Validate the email, allowing for local email addresses
                        if ((authority != null) && (authority.length() != 0) && (!recipients.containsKey(authority)))
                        {
                            if (personExists(authority))
                            {
                                String address = getPersonEmail(authority);
                                if (address != null && address.length() != 0 && validateAddress(address))
                                {
                                    Locale locale = getLocaleForUser(authority);
                                    recipients.put(authority, new Pair<String, Locale>(address, locale));
                                }
                                else
                                {
                                    EmailValidator emailValidator = EmailValidator.getInstance(true);
                                    if (validateAddresses && emailValidator.isValid(authority))
                                    {
                                        Locale locale = getLocaleForUser(authority);
                                        recipients.put(authority, new Pair<String, Locale>(authority, locale));
                                    }
                                }
                            }
                            else
                            {
                                recipients.put(authority, new Pair<String, Locale>(authority, null));
                            }
                        }
                    }
                    else if (authType.equals(AuthorityType.GROUP) || authType.equals(AuthorityType.EVERYONE))
                    {
                        // Notify all members of the group
                        Set<String> users;
                        if (authType.equals(AuthorityType.GROUP))
                        {        
                            users = authorityService.getContainedAuthorities(AuthorityType.USER, authority, false);
                        }
                        else
                        {
                            users = authorityService.getAllAuthorities(AuthorityType.USER);
                        }
                        
                        for (String userAuth : users)
                        {
                            if (recipients.containsKey(userAuth))
                            {
                                continue;
                            }
                            if (personExists(userAuth))
                            {
                                // Check the user name to be a valid email and we don't need to log an error in this case
                                // ALF-19231
                                // Validate the email, allowing for local email addresses
                                String address = getPersonEmail(userAuth);
                                if (address != null && address.length() != 0 && validateAddress(address))
                                {
                                    Locale locale = getLocaleForUser(userAuth);
                                    recipients.put(userAuth, new Pair<String, Locale>(address, locale));
                                }
                                else
                                {
                                    EmailValidator emailValidator = EmailValidator.getInstance(true);
                                    if (validateAddresses && emailValidator.isValid(userAuth))
                                    {
                                        if (userAuth != null && userAuth.length() != 0)
                                        {
                                            Locale locale = getLocaleForUser(userAuth);
                                            recipients.put(userAuth, new Pair<String, Locale>(userAuth, locale));
                                        }
                                    }
                                }
                            }
                            else
                            {
                                recipients.put(userAuth, new Pair<String, Locale>(authority, null));
                            }
                        }
                    }
                }
                if(recipients.size() <= 0)
                {
                    // All recipients were invalid
                    throw new MailPreparationException(
                            "All recipients for the mail action were invalid"
                    );
                }
            }
            else
            {
                // No recipients have been specified
                throw new MailPreparationException(
                        "No recipient has been specified for the mail action"
                );
            }
        }
        return recipients.values();
    }
    
    /**
     * <p>personExists.</p>
     *
     * @param user a {@link java.lang.String} object
     * @return a boolean
     */
    public boolean personExists(final String user)
    {
        boolean exists = false;
        String domain = tenantService.getPrimaryDomain(user); // get primary tenant 
        if (domain != null) 
        { 
            exists = TenantUtil.runAsTenant(new TenantRunAsWork<Boolean>()
            {
                public Boolean doWork() throws Exception
                {
                    return personService.personExists(user);
                }
            }, domain);
        }
        else
        {
            exists = personService.personExists(user);
        }
        return exists;
    }
    
    /**
     * <p>getPerson.</p>
     *
     * @param user a {@link java.lang.String} object
     * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
     */
    public NodeRef getPerson(final String user)
    {
        NodeRef person = null;
        String domain = tenantService.getPrimaryDomain(user); // get primary tenant 
        if (domain != null) 
        { 
            person = TenantUtil.runAsTenant(new TenantRunAsWork<NodeRef>()
            {
                public NodeRef doWork() throws Exception
                {
                    return personService.getPerson(user);
                }
            }, domain);
        }
        else
        {
            person = personService.getPerson(user);
        }
        return person;
    }
    
    /**
     * <p>getPersonEmail.</p>
     *
     * @param user a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    public String getPersonEmail(final String user)
    {
        final NodeRef person = getPerson(user);
        String email = null;
        String domain = tenantService.getPrimaryDomain(user); // get primary tenant 
        if (domain != null) 
        { 
            email = TenantUtil.runAsTenant(new TenantRunAsWork<String>()
            {
                public String doWork() throws Exception
                {
                    return (String) nodeService.getProperty(person, ContentModel.PROP_EMAIL);
                }
            }, domain);
        }
        else
        {
            email = (String) nodeService.getProperty(person, ContentModel.PROP_EMAIL);
        }
        return email;
    }
    
    /**
     * Gets the specified user's preferred locale, if available.
     * 
     * @param user the username of the user whose locale is sought.
     * @return the preferred locale for that user, if available, else <tt>null</tt>. The result would be <tt>null</tt>
     *         e.g. if the user does not exist in the system.
     */
    private Locale getLocaleForUser(final String user)
    {
        Locale locale = null;
        String localeString = null;
        
        // get primary tenant for the specified user.
        //
        // This can have one of (at least) 3 values currently:
        // 1. In single-tenant (community/enterprise) this will be the empty string.
        // 2. In the cloud, for a username such as this: joe.soap@acme.com:
        //    2A. If the acme.com tenant exists in the system, the primary domain is "acme.com"
        //    2B. Id the acme.xom tenant does not exist in the system, the primary domain is null.
        String domain = tenantService.getPrimaryDomain(user);
        
        if (domain != null) 
        { 
            // If the domain is not null, then the user exists in the system and we may get a preferred locale.
            localeString = TenantUtil.runAsSystemTenant(new TenantRunAsWork<String>()
            {
                public String doWork() throws Exception
                {
                    return (String) preferenceService.getPreference(user, "locale");
                }
            }, domain);
        }
        else
        {
            // If the domain is null, then the beahviour here varies depending on whether it's a single tenant or multi-tenant cloud.
            if (personExists(user))
            {
                localeString = AuthenticationUtil.runAsSystem(new RunAsWork<String>()
                {
                    public String doWork() throws Exception 
                    {
                        return (String) preferenceService.getPreference(user, "locale");
                    };
                }); 
            }
            // else leave it as null - there's no tenant, no user for that username, so we can't get a preferred locale.
        }
        
        if (localeString != null)
        {
            locale = StringUtils.parseLocaleString(localeString);
        }

        return locale;
    }
    
    /**
     * Return true if address has valid format
     * @param address String
     * @return boolean
     */
    private boolean validateAddress(String address)
    {
        boolean result = false;
        
        // Validate the email, allowing for local email addresses
        EmailValidator emailValidator = EmailValidator.getInstance(true);
        if (!validateAddresses || emailValidator.isValid(address))
        {
            result = true;
        }
        else 
        {
            logger.error("Failed to send email to '" + address + "' as the address is incorrectly formatted" );
        }
      
        return result;
    }

   /**
    * @param ref    The node representing the current document ref (or null)
    * 
    * @return Model map for email templates
    */
   private Map<String, Object> createEmailTemplateModel(NodeRef ref, Map<String, Object> suppliedModel, NodeRef fromPerson, String toRecipents)
   {
      Map<String, Object> model = new HashMap<String, Object>(8, 1.0f);
      
      if (fromPerson != null)
      {
          model.put("person", new TemplateNode(fromPerson, serviceRegistry, null));
      }

      if (toRecipents != null)
      {
          model.put("to", toRecipents);
      }

      if (ref != null)
      {
          model.put("document", new TemplateNode(ref, serviceRegistry, null));
          NodeRef parent = serviceRegistry.getNodeService().getPrimaryParent(ref).getParentRef();
          model.put("space", new TemplateNode(parent, serviceRegistry, null));
      }
      
      // current date/time is useful to have and isn't supplied by FreeMarker by default
      model.put("date", new Date());
      
      // add custom method objects
      model.put("hasAspect", new HasAspectMethod());
      model.put("message", new I18NMessageMethod());
      model.put("dateCompare", new DateCompareMethod());
      
      // add URLs
      model.put("url", new URLHelper(sysAdminParams));
      model.put(TemplateService.KEY_SHARE_URL, UrlUtil.getShareUrl(this.serviceRegistry.getSysAdminParams()));
      
      //beCPG
      if (attributeService.exists("mail.logo.url")) {
		  Serializable attribute = attributeService.getAttribute("mail.logo.url");
		  mailLogoUrl = (String) attribute;
	  }
      
      if (mailLogoUrl == null || mailLogoUrl.isBlank()) {
    	  mailLogoUrl = UrlUtil.getShareUrl(this.serviceRegistry.getSysAdminParams()) + DEFAULT_MAIL_LOGO_URL;
      }
      
      model.put("mailLogoUrl", mailLogoUrl);
      if (imageResolver != null)
      {
          model.put(TemplateService.KEY_IMAGE_RESOLVER, imageResolver);
      }

      // if the caller specified a model, use it without overriding
      if(suppliedModel != null && suppliedModel.size() > 0)
      {
          for(String key : suppliedModel.keySet())
          {
              if(model.containsKey(key))
              {
                  if(logger.isDebugEnabled())
                  {
                      logger.debug("Not allowing overwriting of built in model parameter " + key);
                  }
              }
              else
              {
                  model.put(key, suppliedModel.get(key));
              }
          }
      }
      
      // all done
      return model;
   }
    
    /**
     * {@inheritDoc}
     *
     * Add the parameter definitions
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_TO, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_TO)));
        paramList.add(new ParameterDefinitionImpl(PARAM_CC, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_CC)));
        paramList.add(new ParameterDefinitionImpl(PARAM_BCC, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_BCC)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TO_MANY, DataTypeDefinition.ANY, false, getParamDisplayLabel(PARAM_TO_MANY), true));
        paramList.add(new ParameterDefinitionImpl(PARAM_SUBJECT, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_SUBJECT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TEXT, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_TEXT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_FROM, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_FROM)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TEMPLATE, DataTypeDefinition.NODE_REF, false, getParamDisplayLabel(PARAM_TEMPLATE), false, "ac-email-templates"));
        paramList.add(new ParameterDefinitionImpl(PARAM_TEMPLATE_MODEL, DataTypeDefinition.ANY, false, getParamDisplayLabel(PARAM_TEMPLATE_MODEL), true));
        paramList.add(new ParameterDefinitionImpl(PARAM_IGNORE_SEND_FAILURE, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_IGNORE_SEND_FAILURE)));
    }

    /** {@inheritDoc} */
    public void setTestMode(boolean testMode)
    {
        this.testMode = testMode;
    }

    /**
     * <p>isTestMode.</p>
     *
     * @return a boolean
     */
    public boolean isTestMode()
    {
        return testMode;
    }

    /**
     * Returns the most recent message that wasn't sent
     *  because TestMode had been enabled.
     *
     * @return a {@link jakarta.mail.internet.MimeMessage} object
     */
    public MimeMessage retrieveLastTestMessage()
    {
        return lastTestMessage; 
    }
    
    /**
     * <p>Getter for the field <code>testSentCount</code>.</p>
     *
     * @return a int
     */
    public int getTestSentCount()
    {
        return testSentCount; 
    }
    
    /**
     * <p>resetTestSentCount.</p>
     *
     * @return a int
     */
    public int resetTestSentCount()
    {
        return testSentCount = 0; 
    }
    
    /**
     * Used when test mode is enabled.
     * Clears the record of the last message that was sent.
     */
    public void clearLastTestMessage()
    {
        lastTestMessage = null;
    }

    /**
     * <p>Setter for the field <code>fromEnabled</code>.</p>
     *
     * @param fromEnabled a boolean
     */
    public void setFromEnabled(boolean fromEnabled)
    {
        this.fromEnabled = fromEnabled;
    }

    /**
     * <p>isFromEnabled.</p>
     *
     * @return a boolean
     */
    public boolean isFromEnabled()
    {
        return fromEnabled;
    }

    /**
     * <p>isHTML.</p>
     *
     * @param value a {@link java.lang.String} object
     * @return a boolean
     */
    public static boolean isHTML(String value)
    {
        // Note: The usage of Apache Tika mimetype detection was considered, but the following cases are detected as text/html:
        // "This is plain text with <html mention inside"
        // "This is plain text\nOnly\nBut it mentions HTML and <html>"
        //  And the following case is detected as "application/xhtml+xml"
        //  "This is plain text with <html xmlns= mention inside"
        // Therefore we decided to not use Tika for html detection.
        boolean result = false;

        // Note: only simplistic matching here - start of the text
        // must be one of <html or <!DOCTYPE
        String htmlPrefix = "<html";
        String dtPrefix = "<!DOCTYPE";
        String trimmedText = value.trim();
        if (trimmedText.length() >= htmlPrefix.length() &&
                trimmedText.substring(0, htmlPrefix.length()).equalsIgnoreCase(htmlPrefix))
        {
            result = true;
        }
        else if (trimmedText.length() >= dtPrefix.length() &&
                trimmedText.substring(0, dtPrefix.length()).equalsIgnoreCase(dtPrefix))
        {
            result = true;
        }

        return result;
    }

    public static class URLHelper
    {
        private final SysAdminParams sysAdminParams;
        
        public URLHelper(SysAdminParams sysAdminParams)
        {
            this.sysAdminParams = sysAdminParams;
        }
        
        public String getContext()
        {
           return "/" + sysAdminParams.getAlfrescoContext();
        }

        public String getServerPath()
        {
            return sysAdminParams.getAlfrescoProtocol() + "://" + sysAdminParams.getAlfrescoHost() + ":"
                    + sysAdminParams.getAlfrescoPort();
        }
    }
    
    static AtomicInteger numberSuccessfulSends = new AtomicInteger(0);
    static AtomicInteger numberFailedSends = new AtomicInteger(0);
    /**
     * <p>onSend.</p>
     */
    protected void onSend()
    {
        numberSuccessfulSends.getAndIncrement();
    }
    
    /**
     * <p>onFail.</p>
     */
    protected void onFail()
    {
        numberFailedSends.getAndIncrement();
    }
    
    /**
     * <p>Getter for the field <code>numberSuccessfulSends</code>.</p>
     *
     * @return a int
     */
    public int getNumberSuccessfulSends()
    {
        return numberSuccessfulSends.get();
    }
        
    /**
     * <p>Getter for the field <code>numberFailedSends</code>.</p>
     *
     * @return a int
     */
    public int getNumberFailedSends()
    {   
        return numberFailedSends.get();
    }
}

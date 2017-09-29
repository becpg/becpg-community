package fr.becpg.repo.ecm.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.repo.ecm.AsyncECOService;
import fr.becpg.repo.ecm.ECOService;
import fr.becpg.repo.mail.BeCPGMailService;

@Service("asyncECOService")
public class AsyncECOServiceImpl implements AsyncECOService {

	public static final String MAIL_TEMPLATE = "/app:company_home/app:dictionary/app:email_templates/cm:asynchrone-actions-email.html.ftl";
	private static final String ARG_ACTION_TYPE = "actionType";
	private static final String ARG_ACTION_STATE = "actionState";
	private static final String ARG_ACTION_DESTINATION = "destination";
	private static final String ARG_ACTION_RUN_TIME = "runTime";
	
	@Autowired
	ECOService ecoService;
	
	@Autowired
	BeCPGMailService beCPGMailService;
	
	@Autowired
	PersonService personService;
	

	private static final Log logger = LogFactory.getLog(AsyncECOServiceImpl.class);

	@Autowired
	@Qualifier("ecoAsyncThreadPool")
	private ThreadPoolExecutor threadExecuter;

	@Autowired
	private TransactionService transactionService;

	@Override
	public void applyAsync(NodeRef ecoNodeRef) {
		runAsync(ecoNodeRef, true);
	}

	@Override
	public void doSimulationAsync(NodeRef ecoNodeRef) {
		runAsync(ecoNodeRef, false);

	}

	private void runAsync(NodeRef ecoNodeRef, boolean apply) {
		Runnable command = new AsyncECOGenerator(ecoNodeRef, apply, AuthenticationUtil.getFullyAuthenticatedUser());
		if (!threadExecuter.getQueue().contains(command)) {
			threadExecuter.execute(command);
		} else {
			logger.warn("AsyncECOGenerator job already in queue for " + ecoNodeRef);
			logger.info("AsyncECOGenerator active task size " + threadExecuter.getActiveCount());
			logger.info("AsyncECOGenerator queue size " + threadExecuter.getTaskCount());
		}			
		
	}

	private class AsyncECOGenerator implements Runnable {

		private final NodeRef ecoNodeRef;		
		private boolean apply = false;
		private final String userName;

		public AsyncECOGenerator(NodeRef ecoNodeRef, boolean apply, String userName) {
			super();
			this.ecoNodeRef = ecoNodeRef;
			this.apply = apply;
			this.userName = userName;
		}

		@Override
		public void run() {
			StopWatch watch = new StopWatch();
			watch.start();
			boolean runState = false;
			
			try {

				AuthenticationUtil.runAs(new RunAsWork<Object>() {

					@Override
					public Object doWork() throws Exception {

						boolean ret = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {

							@Override
							public Boolean execute() throws Throwable {
								return ecoService.setInProgress(ecoNodeRef);
							}
						}, false, true);

						if (ret) {
							transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {

								@Override
								public Boolean execute() throws Throwable {
									if (apply) {
										ecoService.apply(ecoNodeRef);
									} else {
										ecoService.doSimulation(ecoNodeRef);
									}
									return true;

								}
							}, false, true);

						} else {
							logger.warn("ECO already InProgress:" + ecoNodeRef);
						}

						return null;
					}
				}, userName);
				
				runState = true;

			} catch (Exception e) {
				if (e instanceof ConcurrencyFailureException) {
					throw (ConcurrencyFailureException) e;
				}
				logger.error("Unable to apply eco ", e);
			
			} finally {
				// Send mail after ECO
				Map<String, Object> templateArgs = new HashMap<>();
				templateArgs.put(ARG_ACTION_TYPE, "OM");
				templateArgs.put(ARG_ACTION_STATE, runState);
				templateArgs.put(ARG_ACTION_DESTINATION, ecoNodeRef);
				templateArgs.put(ARG_ACTION_RUN_TIME, watch.getTotalTimeSeconds());
				templateArgs.put("apply", apply);
				
				List<NodeRef> recipientNodeRefs = new ArrayList<>();
				recipientNodeRefs.add(personService.getPerson(userName));
				Map<String, Object> templateModel = new HashMap<>();
				templateModel.put("args", templateArgs);
				String subject = "[Notification]" + I18NUtil.getMessage("message.eco-mail.subject");
				
				AuthenticationUtil.runAsSystem(()->{
					beCPGMailService.sendMail(recipientNodeRefs, subject, MAIL_TEMPLATE, templateModel, false);
					return null;
				});
				
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((ecoNodeRef == null) ? 0 : ecoNodeRef.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AsyncECOGenerator other = (AsyncECOGenerator) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (ecoNodeRef == null) {
				if (other.ecoNodeRef != null)
					return false;
			} else if (!ecoNodeRef.equals(other.ecoNodeRef))
				return false;
			return true;
		}

		private AsyncECOServiceImpl getOuterType() {
			return AsyncECOServiceImpl.this;
		}

	}

}

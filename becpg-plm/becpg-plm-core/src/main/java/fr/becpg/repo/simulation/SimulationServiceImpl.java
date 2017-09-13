package fr.becpg.repo.simulation;

import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Service;

import fr.becpg.model.SystemState;

@Service("simulationService")
public class SimulationServiceImpl implements SimulationService {


	@Autowired
	@Qualifier("ecoAsyncThreadPool")
	private ThreadPoolExecutor threadExecuter;
	
	
	@Autowired
	private TransactionService transactionService;

	private static Log logger = LogFactory.getLog(SimulationServiceImpl.class);
	
	@Override
	public void createBudget(NodeRef destNodeRef, SystemState state) {
		
		
		Runnable command = new AsyncCreateBudgetCommand(destNodeRef, state,  AuthenticationUtil.getFullyAuthenticatedUser());
		if (!threadExecuter.getQueue().contains(command)) {
			threadExecuter.execute(command);
		} else {
			logger.warn("AsyncCreateBudgetCommand job already in queue for " + destNodeRef);
			logger.info("AsyncCreateBudgetCommand active task size " + threadExecuter.getActiveCount());
			logger.info("AsyncCreateBudgetCommand queue size " + threadExecuter.getTaskCount());
		}
		
	}
	
	private class AsyncCreateBudgetCommand implements Runnable {

		private final NodeRef destNodeRef;
		private SystemState state = SystemState.Valid;
		private final String userName;

		public AsyncCreateBudgetCommand(NodeRef destNodeRef, SystemState state, String userName) {
			super();
			this.destNodeRef = destNodeRef;
			this.state = state;
			this.userName = userName;
		}

		@Override
		public void run() {
			try {

				AuthenticationUtil.runAs(new RunAsWork<Object>() {

					@Override
					public Object doWork() throws Exception {

						return transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>() {

							@Override
							public Boolean execute() throws Throwable {
								//TODO
								
								
								return null;
							}
						}, false, true);

					}
				}, userName);

			} catch (Exception e) {
				if (e instanceof ConcurrencyFailureException) {
					throw (ConcurrencyFailureException) e;
				}
				logger.error("Unable to create budget ", e);
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((destNodeRef == null) ? 0 : destNodeRef.hashCode());
			result = prime * result + ((state == null) ? 0 : state.hashCode());
			result = prime * result + ((userName == null) ? 0 : userName.hashCode());
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
			AsyncCreateBudgetCommand other = (AsyncCreateBudgetCommand) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (destNodeRef == null) {
				if (other.destNodeRef != null)
					return false;
			} else if (!destNodeRef.equals(other.destNodeRef))
				return false;
			if (state != other.state)
				return false;
			if (userName == null) {
				if (other.userName != null)
					return false;
			} else if (!userName.equals(other.userName))
				return false;
			return true;
		}

		private AsyncCreateBudgetCommand getOuterType() {
			return AsyncCreateBudgetCommand.this;
		}

	}
	
	
}

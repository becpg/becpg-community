package fr.becpg.repo.batch;

import java.io.Serializable;
import java.util.Objects;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

public class BatchInfo implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7569652805526586359L;
	
	public static final int BATCH_THREAD = 3;
	public static final int BATCH_SIZE = 15;

	/**
	 * Will serve as uniq batchId
	 */
	private String batchId;

	/**
	 * Id of the batch will be used assign to threadPool
	 */
	private String batchDescId;

	/**
	 * User that has launch the batch
	 */
	private String batchUser;
	
	private String stepDescId;
	
	private Integer currentStep;
	
	private Integer totalSteps;
	
	private Integer currentItem;
	
	private Integer totalItems;

	private Boolean runAsSystem = Boolean.FALSE;

	private Boolean notifyByMail = Boolean.FALSE;

	private String mailAction;

	private String mailActionUrl;
	
	private String entityDescription;

	private int workerThreads = BATCH_THREAD;

	private int batchSize = BATCH_SIZE;
	
	private Boolean isCompleted = Boolean.FALSE;
	
	private String tenant;
	
	private boolean isCancelled = false;
	
	public static final String BATCH_DESC_ID = "batchDescId";

	public static final String BATCH_USER = "batchUser";
	
	public static final String BATCH_ID = "batchId";
	
	private BatchPriority priority = BatchPriority.MEDIUM;

	public BatchInfo(String batchId, String batchDescId) {
		super();
		this.batchId = batchId;
		this.batchDescId = batchDescId;
		this.batchUser = AuthenticationUtil.getRunAsUser();
		this.tenant = TenantUtil.getCurrentDomain();
	}
	
	public BatchInfo(String batchId, String batchDescId, String entityDescription) {
		this(batchId, batchDescId);
		this.entityDescription = entityDescription;
	}
	
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}
	
	public boolean isCancelled() {
		return isCancelled;
	}
	
	public Integer getCurrentItem() {
		return currentItem;
	}
	
	public void setCurrentItem(Integer currentItem) {
		this.currentItem = currentItem;
	}
	
	public Integer getTotalItems() {
		return totalItems;
	}
	
	public void setTotalItems(Integer totalItems) {
		this.totalItems = totalItems;
	}
	
	public Integer getCurrentStep() {
		return currentStep;
	}
	
	public void setCurrentStep(Integer currentStep) {
		this.currentStep = currentStep;
	}
	
	public Integer getTotalSteps() {
		return totalSteps;
	}
	
	public void setTotalSteps(Integer totalSteps) {
		this.totalSteps = totalSteps;
	}
	
	public String getStepDescId() {
		return stepDescId;
	}
	
	public void setStepDescId(String stepDescId) {
		this.stepDescId = stepDescId;
	}
	
	public int getPriority() {
		return priority.priority();
	}
	
	public void setPriority(BatchPriority priority) {
		this.priority = priority;
	}
	
	public String getTenant() {
		return tenant;
	}
	
	public String getBatchId() {
		return batchId;
	}

	public String getBatchUser() {
		return batchUser;
	}

	public Boolean getRunAsSystem() {
		return runAsSystem;
	}

	public Boolean getNotifyByMail() {
		return notifyByMail;
	}
	
	public String getEntityDescription() {
		return entityDescription;
	}
	
	public void enableNotifyByMail(String mailAction, String mailActionUrl) {
		this.notifyByMail = Boolean.TRUE;
		this.mailAction = mailAction;
		this.mailActionUrl = mailActionUrl;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public void setBatchUser(String batchUser) {
		this.batchUser = batchUser;
	}

	public void setRunAsSystem(Boolean runAsSystem) {
		this.runAsSystem = runAsSystem;
	}

	public String getMailActionUrl() {
		return mailActionUrl;
	}

	public String getBatchDescId() {
		return batchDescId;
	}

	public String getMailAction() {
		return mailAction;
	}

	public int getWorkerThreads() {
		return workerThreads;
	}

	public void setWorkerThreads(int workerThreads) {
		this.workerThreads = workerThreads;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	

	public Boolean getIsCompleted() {
		return isCompleted;
	}

	public void setIsCompleted(Boolean isCompleted) {
		this.isCompleted = isCompleted;
	}

	public JSONObject toJson() {
		JSONObject jsonBatch = new JSONObject();
		jsonBatch.put(BATCH_ID, getBatchId());
		jsonBatch.put(BATCH_USER, getBatchUser());
		String label = I18NUtil.getMessage(getBatchDescId(), entityDescription);

		jsonBatch.put(BATCH_DESC_ID,label!=null ? label :  getBatchDescId());
		
		return jsonBatch;
	}

	@Override
	public int hashCode() {
		return Objects.hash(priority, batchDescId, batchId, batchSize, batchUser, mailAction, mailActionUrl, notifyByMail, runAsSystem, workerThreads);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BatchInfo other = (BatchInfo) obj;
		return Objects.equals(batchDescId, other.batchDescId) && Objects.equals(batchId, other.batchId) && batchSize == other.batchSize
				&& Objects.equals(batchUser, other.batchUser) && Objects.equals(mailAction, other.mailAction)
				&& Objects.equals(mailActionUrl, other.mailActionUrl) && Objects.equals(notifyByMail, other.notifyByMail) && Objects.equals(priority, other.priority)
				&& Objects.equals(runAsSystem, other.runAsSystem) && workerThreads == other.workerThreads;
	}

}

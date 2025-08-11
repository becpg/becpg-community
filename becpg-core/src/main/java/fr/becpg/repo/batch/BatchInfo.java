package fr.becpg.repo.batch;

import java.io.Serializable;
import java.util.Objects;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * <p>BatchInfo class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BatchInfo implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7569652805526586359L;
	
	/** Constant <code>BATCH_THREAD=3</code> */
	public static final int BATCH_THREAD = 3;
	/** Constant <code>BATCH_SIZE=15</code> */
	public static final int BATCH_SIZE = 1;

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
	
	/** Constant <code>BATCH_DESC_ID="batchDescId"</code> */
	public static final String BATCH_DESC_ID = "batchDescId";

	/** Constant <code>BATCH_USER="batchUser"</code> */
	public static final String BATCH_USER = "batchUser";
	
	/** Constant <code>BATCH_ID="batchId"</code> */
	public static final String BATCH_ID = "batchId";
	
	private BatchPriority priority = BatchPriority.MEDIUM;

	/**
	 * <p>Constructor for BatchInfo.</p>
	 *
	 * @param batchId a {@link java.lang.String} object
	 * @param batchDescId a {@link java.lang.String} object
	 */
	public BatchInfo(String batchId, String batchDescId) {
		super();
		this.batchId = batchId;
		this.batchDescId = batchDescId;
		this.batchUser = AuthenticationUtil.getRunAsUser();
		this.tenant = TenantUtil.getCurrentDomain();
	}
	
	/**
	 * <p>Constructor for BatchInfo.</p>
	 *
	 * @param batchId a {@link java.lang.String} object
	 * @param batchDescId a {@link java.lang.String} object
	 * @param entityDescription a {@link java.lang.String} object
	 */
	public BatchInfo(String batchId, String batchDescId, String entityDescription) {
		this(batchId, batchDescId);
		this.entityDescription = entityDescription;
	}
	
	/**
	 * <p>setCancelled.</p>
	 *
	 * @param isCancelled a boolean
	 */
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}
	
	/**
	 * <p>isCancelled.</p>
	 *
	 * @return a boolean
	 */
	public boolean isCancelled() {
		return isCancelled;
	}
	
	/**
	 * <p>Getter for the field <code>currentItem</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object
	 */
	public Integer getCurrentItem() {
		return currentItem;
	}
	
	/**
	 * <p>Setter for the field <code>currentItem</code>.</p>
	 *
	 * @param currentItem a {@link java.lang.Integer} object
	 */
	public void setCurrentItem(Integer currentItem) {
		this.currentItem = currentItem;
	}
	
	/**
	 * <p>Getter for the field <code>totalItems</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object
	 */
	public Integer getTotalItems() {
		return totalItems;
	}
	
	/**
	 * <p>Setter for the field <code>totalItems</code>.</p>
	 *
	 * @param totalItems a {@link java.lang.Integer} object
	 */
	public void setTotalItems(Integer totalItems) {
		this.totalItems = totalItems;
	}
	
	/**
	 * <p>Getter for the field <code>currentStep</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object
	 */
	public Integer getCurrentStep() {
		return currentStep;
	}
	
	/**
	 * <p>Setter for the field <code>currentStep</code>.</p>
	 *
	 * @param currentStep a {@link java.lang.Integer} object
	 */
	public void setCurrentStep(Integer currentStep) {
		this.currentStep = currentStep;
	}
	
	/**
	 * <p>Getter for the field <code>totalSteps</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object
	 */
	public Integer getTotalSteps() {
		return totalSteps;
	}
	
	/**
	 * <p>Setter for the field <code>totalSteps</code>.</p>
	 *
	 * @param totalSteps a {@link java.lang.Integer} object
	 */
	public void setTotalSteps(Integer totalSteps) {
		this.totalSteps = totalSteps;
	}
	
	/**
	 * <p>Getter for the field <code>stepDescId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getStepDescId() {
		return stepDescId;
	}
	
	/**
	 * <p>Setter for the field <code>stepDescId</code>.</p>
	 *
	 * @param stepDescId a {@link java.lang.String} object
	 */
	public void setStepDescId(String stepDescId) {
		this.stepDescId = stepDescId;
	}
	
	/**
	 * <p>Getter for the field <code>priority</code>.</p>
	 *
	 * @return a int
	 */
	public int getPriority() {
		return priority.priority();
	}
	
	/**
	 * <p>Setter for the field <code>priority</code>.</p>
	 *
	 * @param priority a {@link fr.becpg.repo.batch.BatchPriority} object
	 */
	public void setPriority(BatchPriority priority) {
		this.priority = priority;
	}
	
	/**
	 * <p>Getter for the field <code>tenant</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getTenant() {
		return tenant;
	}
	
	/**
	 * <p>Getter for the field <code>batchId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getBatchId() {
		return batchId;
	}

	/**
	 * <p>Getter for the field <code>batchUser</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getBatchUser() {
		return batchUser;
	}

	/**
	 * <p>Getter for the field <code>runAsSystem</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	public Boolean getRunAsSystem() {
		return runAsSystem;
	}

	/**
	 * <p>Getter for the field <code>notifyByMail</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	public Boolean getNotifyByMail() {
		return notifyByMail;
	}
	
	/**
	 * <p>Getter for the field <code>entityDescription</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getEntityDescription() {
		return entityDescription;
	}
	
	/**
	 * <p>enableNotifyByMail.</p>
	 *
	 * @param mailAction a {@link java.lang.String} object
	 * @param mailActionUrl a {@link java.lang.String} object
	 */
	public void enableNotifyByMail(String mailAction, String mailActionUrl) {
		this.notifyByMail = Boolean.TRUE;
		this.mailAction = mailAction;
		this.mailActionUrl = mailActionUrl;
	}

	/**
	 * <p>Setter for the field <code>batchId</code>.</p>
	 *
	 * @param batchId a {@link java.lang.String} object
	 */
	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	/**
	 * <p>Setter for the field <code>batchUser</code>.</p>
	 *
	 * @param batchUser a {@link java.lang.String} object
	 */
	public void setBatchUser(String batchUser) {
		this.batchUser = batchUser;
	}

	/**
	 * <p>Setter for the field <code>runAsSystem</code>.</p>
	 *
	 * @param runAsSystem a {@link java.lang.Boolean} object
	 */
	public void setRunAsSystem(Boolean runAsSystem) {
		this.runAsSystem = runAsSystem;
	}

	/**
	 * <p>Getter for the field <code>mailActionUrl</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getMailActionUrl() {
		return mailActionUrl;
	}

	/**
	 * <p>Getter for the field <code>batchDescId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getBatchDescId() {
		return batchDescId;
	}

	/**
	 * <p>Getter for the field <code>mailAction</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getMailAction() {
		return mailAction;
	}

	/**
	 * <p>Getter for the field <code>workerThreads</code>.</p>
	 *
	 * @return a int
	 */
	public int getWorkerThreads() {
		return workerThreads;
	}

	/**
	 * <p>Setter for the field <code>workerThreads</code>.</p>
	 *
	 * @param workerThreads a int
	 */
	public void setWorkerThreads(int workerThreads) {
		this.workerThreads = workerThreads;
	}

	/**
	 * <p>Getter for the field <code>batchSize</code>.</p>
	 *
	 * @return a int
	 */
	public int getBatchSize() {
		return batchSize;
	}

	/**
	 * <p>Setter for the field <code>batchSize</code>.</p>
	 *
	 * @param batchSize a int
	 */
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	

	/**
	 * <p>Getter for the field <code>isCompleted</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	public Boolean getIsCompleted() {
		return isCompleted;
	}

	/**
	 * <p>Setter for the field <code>isCompleted</code>.</p>
	 *
	 * @param isCompleted a {@link java.lang.Boolean} object
	 */
	public void setIsCompleted(Boolean isCompleted) {
		this.isCompleted = isCompleted;
	}

	/**
	 * <p>toJson.</p>
	 *
	 * @return a {@link org.json.JSONObject} object
	 */
	public JSONObject toJson() {
		JSONObject jsonBatch = new JSONObject();
		jsonBatch.put(BATCH_ID, getBatchId());
		jsonBatch.put(BATCH_USER, getBatchUser());
		String label = I18NUtil.getMessage(getBatchDescId(), entityDescription);

		jsonBatch.put(BATCH_DESC_ID,label!=null ? label :  getBatchDescId());
		
		return jsonBatch;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(priority, batchDescId, batchId, batchSize, batchUser, mailAction, mailActionUrl, notifyByMail, runAsSystem, workerThreads);
	}

	/** {@inheritDoc} */
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

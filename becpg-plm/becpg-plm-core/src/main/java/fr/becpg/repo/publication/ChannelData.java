package fr.becpg.repo.publication;

import java.util.Date;

/**
 * Carries remote publication channel batch values between web scripts and the service layer.
 */
public class ChannelData {

	private final String status;
	private final String batchId;
	private final Integer failCount;
	private final Integer readCount;
	private final String error;
	private final String lastSuccessBatchId;
	private final Date lastDate;
	private final String action;
	private final Date modifiedDate;

	private ChannelData(Builder builder) {
		this.status = builder.status;
		this.batchId = builder.batchId;
		this.failCount = builder.failCount;
		this.readCount = builder.readCount;
		this.error = builder.error;
		this.lastSuccessBatchId = builder.lastSuccessBatchId;
		this.lastDate = builder.lastDate;
		this.action = builder.action;
		this.modifiedDate = builder.modifiedDate;
	}

	/**
	 * Creates a new builder instance.
	 *
	 * @return a new builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Returns the status.
	 *
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Returns the batch identifier.
	 *
	 * @return the batch identifier
	 */
	public String getBatchId() {
		return batchId;
	}

	/**
	 * Returns the fail count.
	 *
	 * @return the fail count
	 */
	public Integer getFailCount() {
		return failCount;
	}

	/**
	 * Returns the read count.
	 *
	 * @return the read count
	 */
	public Integer getReadCount() {
		return readCount;
	}

	/**
	 * Returns the error message.
	 *
	 * @return the error message
	 */
	public String getError() {
		return error;
	}

	/**
	 * Returns the last successful batch identifier.
	 *
	 * @return the last successful batch identifier
	 */
	public String getLastSuccessBatchId() {
		return lastSuccessBatchId;
	}

	/**
	 * Returns the last publication date.
	 *
	 * @return the last publication date
	 */
	public Date getLastDate() {
		return lastDate;
	}

	/**
	 * Returns the action.
	 *
	 * @return the action
	 */
	public String getAction() {
		return action;
	}
	
	public Date getModifiedDate() {
		return modifiedDate;
	}

	/**
	 * Builder for {@link ChannelData}.
	 */
	public static class Builder {

		private String status;
		private String batchId;
		private Integer failCount;
		private Integer readCount;
		private String error;
		private String lastSuccessBatchId;
		private Date lastDate;
		private String action;
		private Date modifiedDate;

		private Builder() {
		}

		/**
		 * Sets the status.
		 *
		 * @param status the status
		 * @return the builder
		 */
		public Builder status(String status) {
			this.status = status;
			return this;
		}

		/**
		 * Sets the batch identifier.
		 *
		 * @param batchId the batch identifier
		 * @return the builder
		 */
		public Builder batchId(String batchId) {
			this.batchId = batchId;
			return this;
		}
		
		public Builder modifiedDate(Date modifiedDate) {
			this.modifiedDate = modifiedDate;
			return this;
		}

		/**
		 * Sets the fail count.
		 *
		 * @param failCount the fail count
		 * @return the builder
		 */
		public Builder failCount(Integer failCount) {
			this.failCount = failCount;
			return this;
		}

		/**
		 * Sets the read count.
		 *
		 * @param readCount the read count
		 * @return the builder
		 */
		public Builder readCount(Integer readCount) {
			this.readCount = readCount;
			return this;
		}

		/**
		 * Sets the error message.
		 *
		 * @param error the error message
		 * @return the builder
		 */
		public Builder error(String error) {
			this.error = error;
			return this;
		}

		/**
		 * Sets the last successful batch identifier.
		 *
		 * @param lastSuccessBatchId the last successful batch identifier
		 * @return the builder
		 */
		public Builder lastSuccessBatchId(String lastSuccessBatchId) {
			this.lastSuccessBatchId = lastSuccessBatchId;
			return this;
		}

		/**
		 * Sets the last publication date.
		 *
		 * @param lastDate the last publication date
		 * @return the builder
		 */
		public Builder lastDate(Date lastDate) {
			this.lastDate = lastDate;
			return this;
		}

		/**
		 * Sets the action.
		 *
		 * @param action the action
		 * @return the builder
		 */
		public Builder action(String action) {
			this.action = action;
			return this;
		}

		/**
		 * Builds the channel context.
		 *
		 * @return the channel context
		 */
		public ChannelData build() {
			return new ChannelData(this);
		}
	}
}

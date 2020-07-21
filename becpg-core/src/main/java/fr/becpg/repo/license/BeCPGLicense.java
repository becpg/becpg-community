package fr.becpg.repo.license;


/**
 * <p>BeCPGLicense class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BeCPGLicense {

	String licenseName = "beCPG OO License";
	long allowedConcurrentRead = -1L;
	long allowedConcurrentWrite = -1L;
	long allowedConcurrentSupplier = -1L;
	long allowedNamedWrite = -1L;
	long allowedNamedRead = -1L;
	
	

	/**
	 * <p>Constructor for BeCPGLicense.</p>
	 *
	 * @param licenseName a {@link java.lang.String} object.
	 * @param allowedConcurrentRead a long.
	 * @param allowedConcurrentWrite a long.
	 * @param allowedConcurrentSupplier a long.
	 * @param allowedNamedWrite a long.
	 * @param allowedNamedRead a long.
	 */
	public BeCPGLicense(String licenseName, long allowedConcurrentRead, long allowedConcurrentWrite, long allowedConcurrentSupplier,
			long allowedNamedWrite, long allowedNamedRead) {
		super();
		this.licenseName = licenseName;
		this.allowedConcurrentRead = allowedConcurrentRead;
		this.allowedConcurrentWrite = allowedConcurrentWrite;
		this.allowedConcurrentSupplier = allowedConcurrentSupplier;
		this.allowedNamedWrite = allowedNamedWrite;
		this.allowedNamedRead = allowedNamedRead;
	}

	/**
	 * <p>Constructor for BeCPGLicense.</p>
	 */
	public BeCPGLicense() {
		// TODO Auto-generated constructor stub
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (allowedConcurrentRead ^ (allowedConcurrentRead >>> 32));
		result = prime * result + (int) (allowedConcurrentSupplier ^ (allowedConcurrentSupplier >>> 32));
		result = prime * result + (int) (allowedConcurrentWrite ^ (allowedConcurrentWrite >>> 32));
		result = prime * result + (int) (allowedNamedRead ^ (allowedNamedRead >>> 32));
		result = prime * result + (int) (allowedNamedWrite ^ (allowedNamedWrite >>> 32));
		result = prime * result + ((licenseName == null) ? 0 : licenseName.hashCode());
		return result;
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
		BeCPGLicense other = (BeCPGLicense) obj;
		if (allowedConcurrentRead != other.allowedConcurrentRead)
			return false;
		if (allowedConcurrentSupplier != other.allowedConcurrentSupplier)
			return false;
		if (allowedConcurrentWrite != other.allowedConcurrentWrite)
			return false;
		if (allowedNamedRead != other.allowedNamedRead)
			return false;
		if (allowedNamedWrite != other.allowedNamedWrite)
			return false;
		if (licenseName == null) {
			if (other.licenseName != null)
				return false;
		} else if (!licenseName.equals(other.licenseName))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "License [licenseName=" + licenseName + ", allowedConcurrentRead=" + allowedConcurrentRead + ", allowedConcurrentWrite="
				+ allowedConcurrentWrite + ", allowedConcurrentSupplier=" + allowedConcurrentSupplier + ", allowedNamedWrite=" + allowedNamedWrite
				+ ", allowedNamedRead=" + allowedNamedRead + "]";
	}


}

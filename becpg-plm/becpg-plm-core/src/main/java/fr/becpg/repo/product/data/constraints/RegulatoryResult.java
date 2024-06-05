package fr.becpg.repo.product.data.constraints;

/**
 * <p>RegulatoryResult class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public enum RegulatoryResult {
		ERROR(3),
	    PROHIBITED(2),
	    PERMITTED(1),
	    NOT_LISTED(0);

	    private final int severity;

	    RegulatoryResult(int severity) {
	        this.severity = severity;
	    }

	    /**
	     * <p>Getter for the field <code>severity</code>.</p>
	     *
	     * @return a int
	     */
	    public int getSeverity() {
	        return severity;
	    }
	 
	
}

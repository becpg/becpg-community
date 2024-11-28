package fr.becpg.repo.entity.simulation;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>EntitySimulationPlugin interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface EntitySimulationPlugin {

	/** Constant <code>DATALIST_MODE="datalist"</code> */
	final static String DATALIST_MODE = "datalist";
	/** Constant <code>SIMPLE_MODE="simple"</code> */
	final static String SIMPLE_MODE = "simple";
	/** Constant <code>RECUR_MODE="recur"</code> */
	final static String RECUR_MODE = "recur";
		
	/**
	 * <p>accept.</p>
	 *
	 * @param simulationMode a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	boolean accept(String simulationMode);

	/**
	 * <p>simulateNodeRefs.</p>
	 *
	 * @param destNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param nodeRefs a {@link java.util.List} object.
	 * @param branch a boolean
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> simulateNodeRefs(NodeRef destNodeRef, List<NodeRef> nodeRefs, boolean branch);
	
	/**
	 * <p>simulateNodeRefs.</p>
	 *
	 * @param destNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param nodeRefs a {@link java.util.List} object.
	 * @return a {@link java.util.List} object.
	 */
	default List<NodeRef> simulateNodeRefs(NodeRef destNodeRef, List<NodeRef> nodeRefs) {
		return simulateNodeRefs(destNodeRef, nodeRefs, true);
	}
}

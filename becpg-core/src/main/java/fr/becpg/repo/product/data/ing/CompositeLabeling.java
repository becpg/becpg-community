/*
 * 
 */
package fr.becpg.repo.product.data.ing;

import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.ProductData;

//TODO voir pour faire mieux avec les heritages Composite<LabelingComponent>
public class CompositeLabeling extends AbstractLabelingComponent {

	private Map<NodeRef, AbstractLabelingComponent> ingList = new LinkedHashMap<NodeRef, AbstractLabelingComponent>();

	private Map<NodeRef, AbstractLabelingComponent> ingListNotDeclared = new LinkedHashMap<NodeRef, AbstractLabelingComponent>();

	private Double qtyRMUsed = 0d;
	
	private boolean isGroup = false;

	public CompositeLabeling(ProductData productData) {
		//TODO c null !!
		this.name = productData.getName();
		this.nodeRef = productData.getNodeRef();
		this.legalName = productData.getLegalName();
		
	}

	public CompositeLabeling() {
		super();
	}
	
	


	public boolean isGroup() {
		return isGroup;
	}

	public void setGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}

	public Double getQtyRMUsed() {
		return qtyRMUsed;
	}

	public void setQtyRMUsed(Double qtyRMUsed) {
		this.qtyRMUsed = qtyRMUsed;
	}

	public void add(AbstractLabelingComponent ing, boolean isDeclared) {
		if (isDeclared) {
			ingList.put(ing.getNodeRef(), ing);
		} else {
			ingListNotDeclared.put(ing.getNodeRef(), ing);
		}
	}

	public void remove(NodeRef ing, boolean isDeclared) {
		if (isDeclared) {
			ingList.remove(ing);
		} else {
			ingListNotDeclared.remove(ing);
		}
	}

	public AbstractLabelingComponent get(NodeRef grpNodeRef, boolean isDeclared) {

		AbstractLabelingComponent ing = null;
		if (isDeclared) {
			ing = ingList.get(grpNodeRef);
		} else {
			ing = ingListNotDeclared.get(grpNodeRef);
		}

		return ing;
	}

	public Map<NodeRef, AbstractLabelingComponent> getIngList() {
		return ingList;
	}

	public Map<NodeRef, AbstractLabelingComponent> getIngListNotDeclared() {
		return ingListNotDeclared;
	}


}

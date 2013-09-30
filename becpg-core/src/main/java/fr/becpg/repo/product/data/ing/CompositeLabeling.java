/*
 * 
 */
package fr.becpg.repo.product.data.ing;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.product.data.ProductData;

//TODO voir pour faire mieux avec les heritages Composite<LabelingComponent>
public class CompositeLabeling extends AbstractLabelingComponent {

	private Map<NodeRef, AbstractLabelingComponent> ingList = new LinkedHashMap<NodeRef, AbstractLabelingComponent>();


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

	public void add(AbstractLabelingComponent ing) {
		ingList.put(ing.getNodeRef(), ing);
		
	}

	public void remove(NodeRef ing) {
		ingList.remove(ing);
	}

	public AbstractLabelingComponent get(NodeRef grpNodeRef) {
		return ingList.get(grpNodeRef);

	}

	public Map<NodeRef, AbstractLabelingComponent> getIngList() {
		return ingList;
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		print(sb, "",true);
		return sb.toString();
	}

	private void print(StringBuilder sb, String prefix, boolean isTail) {
		sb.append(prefix + (isTail ? "└──[" : "├──[")+ (getLegalName(I18NUtil.getContentLocaleLang())==null ? "root" : getLegalName(I18NUtil.getContentLocaleLang()))+" - "+getQty()+" ("+getQtyRMUsed()+")"  +"]\n");
        for (Iterator<AbstractLabelingComponent> iterator = ingList.values().iterator(); iterator.hasNext(); ) {
        	AbstractLabelingComponent labelingComponent =  iterator.next();
        	if(labelingComponent  instanceof CompositeLabeling) {
				((CompositeLabeling)labelingComponent).print(sb, prefix + (isTail ? "    " : "│   "), !iterator.hasNext());
			} else {
				sb.append(prefix + (isTail ? "    " : "│   ") +(!iterator.hasNext() ? "└──[" : "├──[")+ labelingComponent.getLegalName(I18NUtil.getContentLocaleLang())+" - "+labelingComponent.getQty()  +"]\n");
			      
			}
 
        }
    }
	

}

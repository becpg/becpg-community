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
import fr.becpg.repo.product.data.constraints.DeclarationType;

//TODO voir pour faire mieux avec les heritages Composite<LabelingComponent>
public class CompositeLabeling extends AbstractLabelingComponent {

	private Map<NodeRef, AbstractLabelingComponent> ingList = new LinkedHashMap<NodeRef, AbstractLabelingComponent>();

	private Double qtyRMUsed = 0d;
	
	private IngTypeItem ingType;
	
	private DeclarationType declarationType;
	

	public CompositeLabeling() {
		super();
	}
	
	public CompositeLabeling(CompositeLabeling compositeLabeling) 
	{
		super(compositeLabeling);
		this.ingType = compositeLabeling.ingType;
	    this.ingList = compositeLabeling.ingList;
	    this.qtyRMUsed = compositeLabeling.qtyRMUsed;
	    this.declarationType = compositeLabeling.declarationType;
	}
	

	public CompositeLabeling(ProductData productData) {
		this.name = productData.getName();
		this.nodeRef = productData.getNodeRef();
		this.legalName = productData.getLegalName();
		this.ingType = productData.getIngType();
	}

	
	public IngTypeItem getIngType() {
		return ingType;
	}

	public void setIngType(IngTypeItem ingType) {
		this.ingType = ingType;
	}

	public DeclarationType getDeclarationType() {
		return declarationType;
	}

	public void setDeclarationType(DeclarationType declarationType) {
		this.declarationType = declarationType;
	}

	public boolean isGroup() {
		return DeclarationType.Group.equals(declarationType);
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
		sb.append(prefix + (isTail ? "└──[" : "├──[")+ (getLegalName(I18NUtil.getContentLocaleLang())==null ? "root" : getLegalName(I18NUtil.getContentLocaleLang()))+" - "+getQty()+" ("+getQtyRMUsed()+", vol: "+getVolumeQtyPerc()+") "+(declarationType!=null ? declarationType.toString():"")+"]\n");
        for (Iterator<AbstractLabelingComponent> iterator = ingList.values().iterator(); iterator.hasNext(); ) {
        	AbstractLabelingComponent labelingComponent =  iterator.next();
        	if(labelingComponent  instanceof CompositeLabeling) {
				((CompositeLabeling)labelingComponent).print(sb, prefix + (isTail ? "    " : "│   "), !iterator.hasNext());
			} else {
				sb.append(prefix + (isTail ? "    " : "│   ") +(!iterator.hasNext() ? "└──[" : "├──[")+ labelingComponent.getLegalName(I18NUtil.getContentLocaleLang())+" - "+labelingComponent.getQty() +" ( vol : "+labelingComponent.getVolumeQtyPerc()+") ]\n");
			      
			}
 
        }
    }

	

}

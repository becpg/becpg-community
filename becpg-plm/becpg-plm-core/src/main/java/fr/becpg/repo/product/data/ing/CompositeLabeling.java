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

	public final static String ROOT = "root";
	
	private static final long serialVersionUID = 7903326038199131582L;

	private Map<NodeRef, AbstractLabelingComponent> ingList = new LinkedHashMap<>();

	private Double qtyTotal = 0d;
	
	private Double volumeTotal = 0d; 
	
	
	private IngTypeItem ingType;
	
	private DeclarationType declarationType;
	
	public CompositeLabeling(String name) {
		super();
		this.name = name;
	}
	
	public CompositeLabeling() {
		super();
	}
	
	public CompositeLabeling(CompositeLabeling compositeLabeling) 
	{
		super(compositeLabeling);
		this.ingType = compositeLabeling.ingType;
	    this.ingList = clone(compositeLabeling.ingList);
	    this.qtyTotal = compositeLabeling.qtyTotal;
	    this.volumeTotal = compositeLabeling.volumeTotal;
	    this.declarationType = compositeLabeling.declarationType;
	}
	

	public CompositeLabeling(ProductData productData) {
		this.name = productData.getName();
		this.nodeRef = productData.getNodeRef();
		this.legalName = productData.getLegalName();
		this.pluralLegalName = productData.getPluralLegalName();
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
		return DeclarationType.Group.equals(declarationType) ||  DeclarationType.Kit.equals(declarationType);
	}

	public Double getQtyTotal() {
		return qtyTotal;
	}

	public void setQtyTotal(Double qtyTotal) {
		this.qtyTotal = qtyTotal;
	}

	public Double getVolumeTotal() {
		return volumeTotal;
	}

	public void setVolumeTotal(Double volumeTotal) {
		this.volumeTotal = volumeTotal;
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
	
	public void setIngList(Map<NodeRef, AbstractLabelingComponent> ingList) {
		this.ingList = ingList;
	}

	@Override
	public CompositeLabeling clone() {
		return new CompositeLabeling(this);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		print(sb, "",true);
		return sb.toString();
	}

	private void print(StringBuilder sb, String prefix, boolean isTail) {
		sb.append(prefix).append(isTail ? "└──[" : "├──[").append(getLegalName(I18NUtil.getContentLocaleLang()) == null ? ROOT : getLegalName(I18NUtil.getContentLocaleLang())).append(" ( plural:"+isPlural()+",shouldSkip:"+shouldSkip()+") ").append(" - ").append(getQty()).append(" (").append(getQtyTotal()).append(", vol: ").append(getVolumeTotal()).append(") ").append(declarationType != null ? declarationType.toString() : "").append("]\n");
        for (Iterator<AbstractLabelingComponent> iterator = ingList.values().iterator(); iterator.hasNext(); ) {
        	AbstractLabelingComponent labelingComponent =  iterator.next();
        	if(labelingComponent  instanceof CompositeLabeling) {
				((CompositeLabeling)labelingComponent).print(sb, prefix + (isTail ? "    " : "│   "), !iterator.hasNext());
			} else {
				sb.append(prefix).append(isTail ? "    " : "│   ").append(!iterator.hasNext() ? "└──[" : "├──[").append(labelingComponent.getLegalName(I18NUtil.getContentLocaleLang())).append(" ( plural:"+labelingComponent.isPlural()+",shouldSkip:"+labelingComponent.shouldSkip()+") ").append(" - ").append(labelingComponent.getQty()).append(" ( vol : ").append(labelingComponent.getVolume()).append(") ]\n"); 
			}
 
        }
    }

	

}

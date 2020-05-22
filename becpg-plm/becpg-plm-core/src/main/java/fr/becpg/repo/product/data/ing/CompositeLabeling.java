/*
 *
 */
package fr.becpg.repo.product.data.ing;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;

//TODO voir pour faire mieux avec les heritages Composite<LabelingComponent>
public class CompositeLabeling extends LabelingComponent {

	public final static String ROOT = "root";

	private static final long serialVersionUID = 7903326038199131582L;

	private Map<NodeRef, CompositeLabeling> ingList = new LinkedHashMap<>();

	private Map<NodeRef, CompositeLabeling> ingListAtEnd = new LinkedHashMap<>();

	private Double qtyTotal = 0d;
	
	private Double evaporatingLoss = 0d;

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

	public CompositeLabeling(CompositeLabeling compositeLabeling) {
		super(compositeLabeling);
		this.ingType = compositeLabeling.ingType;
		this.ingList = clone(compositeLabeling.ingList);
		this.qtyTotal = compositeLabeling.qtyTotal;
		this.evaporatingLoss = compositeLabeling.evaporatingLoss;
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

	@SuppressWarnings("unchecked")
	protected <T extends CompositeLabeling> List<T> clone(List<T> list) {
		List<T> ret = new LinkedList<>();
		if (list != null) {
			for (T toAdd : list) {
				ret.add((T) toAdd.clone());
			}
		}
		return ret;
	}

	protected Map<NodeRef, CompositeLabeling> clone(Map<NodeRef, CompositeLabeling> list) {
		Map<NodeRef, CompositeLabeling> ret = new LinkedHashMap<>();
		if (list != null) {
			for (Map.Entry<NodeRef, CompositeLabeling> toAdd : list.entrySet()) {
				ret.put(toAdd.getKey(), toAdd.getValue().clone());
			}
		}
		return ret;
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
		return DeclarationType.Group.equals(declarationType) || DeclarationType.Kit.equals(declarationType);
	}

	public Double getQtyTotal() {
		return qtyTotal;
	}

	public void setQtyTotal(Double qtyTotal) {
		this.qtyTotal = qtyTotal;
	}

	public Double getEvaporatingLoss() {
		return evaporatingLoss;
	}

	public void setEvaporatingLoss(Double evaporatingLoss) {
		this.evaporatingLoss = evaporatingLoss;
	}

	public Double getVolumeTotal() {
		return volumeTotal;
	}

	public void setVolumeTotal(Double volumeTotal) {
		this.volumeTotal = volumeTotal;
	}

	public void add(CompositeLabeling ing) {
		ingList.put(ing.getNodeRef(), ing);
	}

	public void addAtEnd(CompositeLabeling ing) {
		ingListAtEnd.put(ing.getNodeRef(), ing);
	}

	public void remove(NodeRef ing) {
		ingList.remove(ing);
	}

	public CompositeLabeling get(NodeRef grpNodeRef) {
		return ingList.get(grpNodeRef);
	}

	public CompositeLabeling getAtEnd(NodeRef grpNodeRef) {
		return ingListAtEnd.get(grpNodeRef);
	}

	public void removeAtEnd(NodeRef ing) {
		ingListAtEnd.remove(ing);

	}

	public Map<NodeRef, CompositeLabeling> getIngListAtEnd() {
		return ingListAtEnd;
	}

	public void setIngListAtEnd(Map<NodeRef, CompositeLabeling> ingListAtEnd) {
		this.ingListAtEnd = ingListAtEnd;
	}

	public Map<NodeRef, CompositeLabeling> getIngList() {
		return ingList;
	}

	public void setIngList(Map<NodeRef, CompositeLabeling> ingList) {
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
		print(sb, "", true);
		return sb.toString();
	}

	private void print(StringBuilder sb, String prefix, boolean isTail) {
		sb.append(prefix).append(isTail ? "└──[" : "├──[")
				.append(getLegalName(I18NUtil.getContentLocaleLang()) == null ? ROOT : getLegalName(I18NUtil.getContentLocaleLang()))
				.append(" ( plural:" + isPlural() + ") ").append(" - ").append(getQty()).append(" (").append(getQtyTotal()).append(", vol: ")
				.append(getVolumeTotal()).append(") ").append(declarationType != null ? declarationType.toString() : "").append("]\n");
		for (Iterator<CompositeLabeling> iterator = ingList.values().iterator(); iterator.hasNext();) {
			CompositeLabeling labelingComponent = iterator.next();
			if (labelingComponent instanceof CompositeLabeling) {
				labelingComponent.print(sb, prefix + (isTail ? "    " : "│   "), !iterator.hasNext());
			} else {
				sb.append(prefix).append(isTail ? "    " : "│   ").append(!iterator.hasNext() ? "└──[" : "├──[")
						.append(labelingComponent.getLegalName(I18NUtil.getContentLocaleLang()))
						.append(" ( plural:" + labelingComponent.isPlural() + " ) ").append(" - ").append(labelingComponent.getQty())
						.append(" ( vol : ").append(labelingComponent.getVolume()).append(") ]\n");
			}

		}
		for (Iterator<CompositeLabeling> iterator = ingListAtEnd.values().iterator(); iterator.hasNext();) {
			CompositeLabeling labelingComponent = iterator.next();
			if (labelingComponent instanceof CompositeLabeling) {
				labelingComponent.print(sb, prefix + (isTail ? "    " : "│   "), !iterator.hasNext());
			} else {
				sb.append(prefix).append(isTail ? "    " : "│   ").append(!iterator.hasNext() ? "*──[" : "├──[")
						.append(labelingComponent.getLegalName(I18NUtil.getContentLocaleLang()))
						.append(" ( plural:" + labelingComponent.isPlural() + " ) ")
						.append(" ]\n");
			}

		}
		
		
	}

}

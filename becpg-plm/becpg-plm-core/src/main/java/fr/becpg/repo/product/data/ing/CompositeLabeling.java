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
/**
 * <p>CompositeLabeling class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CompositeLabeling extends LabelingComponent {

	/** Constant <code>ROOT="root"</code> */
	public static final String ROOT = "root";

	private static final long serialVersionUID = 7903326038199131582L;

	private Map<NodeRef, CompositeLabeling> ingList = new LinkedHashMap<>();

	private Map<NodeRef, CompositeLabeling> ingListAtEnd = new LinkedHashMap<>();

	private Double qtyTotal = 0d;
	
	private Double evaporatingLoss = 0d;

	private Double volumeTotal = 0d;

	private IngTypeItem ingType;

	private DeclarationType declarationType;
	
	/**
	 * <p>Constructor for CompositeLabeling.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public CompositeLabeling(String name) {
		super();
		this.name = name;
	}

	/**
	 * <p>Constructor for CompositeLabeling.</p>
	 */
	public CompositeLabeling() {
		super();
	}

	/**
	 * <p>Constructor for CompositeLabeling.</p>
	 *
	 * @param compositeLabeling a {@link fr.becpg.repo.product.data.ing.CompositeLabeling} object.
	 */
	public CompositeLabeling(CompositeLabeling compositeLabeling) {
		super(compositeLabeling);
		this.ingType = compositeLabeling.ingType;
		this.ingList = clone(compositeLabeling.ingList);
		this.ingListAtEnd = clone(compositeLabeling.ingListAtEnd);
		this.qtyTotal = compositeLabeling.qtyTotal;
		this.evaporatingLoss = compositeLabeling.evaporatingLoss;
		this.volumeTotal = compositeLabeling.volumeTotal;
		this.declarationType = compositeLabeling.declarationType;
	}

	/**
	 * <p>Constructor for CompositeLabeling.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object.
	 */
	public CompositeLabeling(ProductData productData) {
		this.name = productData.getName();
		this.nodeRef = productData.getNodeRef();
		this.legalName = productData.getLegalName();
		this.pluralLegalName = productData.getPluralLegalName();
		this.ingType = productData.getIngType();

	}

	/**
	 * <p>clone.</p>
	 *
	 * @param list a {@link java.util.List} object.
	 * @return a {@link java.util.List} object.
	 */
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

	/**
	 * <p>clone.</p>
	 *
	 * @param list a {@link java.util.Map} object.
	 * @return a {@link java.util.Map} object.
	 */
	protected Map<NodeRef, CompositeLabeling> clone(Map<NodeRef, CompositeLabeling> list) {
		Map<NodeRef, CompositeLabeling> ret = new LinkedHashMap<>();
		if (list != null) {
			for (Map.Entry<NodeRef, CompositeLabeling> toAdd : list.entrySet()) {
				ret.put(toAdd.getKey(), toAdd.getValue().clone());
			}
		}
		return ret;
	}

	

	/**
	 * <p>Getter for the field <code>ingType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.ing.IngTypeItem} object.
	 */
	public IngTypeItem getIngType() {
		return ingType;
	}

	/**
	 * <p>Setter for the field <code>ingType</code>.</p>
	 *
	 * @param ingType a {@link fr.becpg.repo.product.data.ing.IngTypeItem} object.
	 */
	public void setIngType(IngTypeItem ingType) {
		this.ingType = ingType;
	}

	/**
	 * <p>Getter for the field <code>declarationType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.DeclarationType} object.
	 */
	public DeclarationType getDeclarationType() {
		return declarationType;
	}

	/**
	 * <p>Setter for the field <code>declarationType</code>.</p>
	 *
	 * @param declarationType a {@link fr.becpg.repo.product.data.constraints.DeclarationType} object.
	 */
	public void setDeclarationType(DeclarationType declarationType) {
		this.declarationType = declarationType;
	}

	/**
	 * <p>isGroup.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isGroup() {
		return DeclarationType.Group.equals(declarationType) || DeclarationType.Kit.equals(declarationType);
	}

	/**
	 * <p>Getter for the field <code>qtyTotal</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getQtyTotal() {
		return qtyTotal;
	}

	/**
	 * <p>Setter for the field <code>qtyTotal</code>.</p>
	 *
	 * @param qtyTotal a {@link java.lang.Double} object.
	 */
	public void setQtyTotal(Double qtyTotal) {
		this.qtyTotal = qtyTotal;
	}

	/**
	 * <p>Getter for the field <code>evaporatingLoss</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getEvaporatingLoss() {
		return evaporatingLoss;
	}

	/**
	 * <p>Setter for the field <code>evaporatingLoss</code>.</p>
	 *
	 * @param evaporatingLoss a {@link java.lang.Double} object.
	 */
	public void setEvaporatingLoss(Double evaporatingLoss) {
		this.evaporatingLoss = evaporatingLoss;
	}

	/**
	 * <p>Getter for the field <code>volumeTotal</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getVolumeTotal() {
		return volumeTotal;
	}

	/**
	 * <p>Setter for the field <code>volumeTotal</code>.</p>
	 *
	 * @param volumeTotal a {@link java.lang.Double} object.
	 */
	public void setVolumeTotal(Double volumeTotal) {
		this.volumeTotal = volumeTotal;
	}

	/**
	 * <p>add.</p>
	 *
	 * @param ing a {@link fr.becpg.repo.product.data.ing.CompositeLabeling} object.
	 */
	public void add(CompositeLabeling ing) {
		ingList.put(ing.getNodeRef(), ing);
	}

	/**
	 * <p>addAtEnd.</p>
	 *
	 * @param ing a {@link fr.becpg.repo.product.data.ing.CompositeLabeling} object.
	 */
	public void addAtEnd(CompositeLabeling ing) {
		ingListAtEnd.put(ing.getNodeRef(), ing);
	}

	/**
	 * <p>remove.</p>
	 *
	 * @param ing a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void remove(NodeRef ing) {
		ingList.remove(ing);
	}

	/**
	 * <p>get.</p>
	 *
	 * @param grpNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link fr.becpg.repo.product.data.ing.CompositeLabeling} object.
	 */
	public CompositeLabeling get(NodeRef grpNodeRef) {
		return ingList.get(grpNodeRef);
	}

	/**
	 * <p>getAtEnd.</p>
	 *
	 * @param grpNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link fr.becpg.repo.product.data.ing.CompositeLabeling} object.
	 */
	public CompositeLabeling getAtEnd(NodeRef grpNodeRef) {
		return ingListAtEnd.get(grpNodeRef);
	}

	/**
	 * <p>removeAtEnd.</p>
	 *
	 * @param ing a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void removeAtEnd(NodeRef ing) {
		ingListAtEnd.remove(ing);

	}

	/**
	 * <p>Getter for the field <code>ingListAtEnd</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<NodeRef, CompositeLabeling> getIngListAtEnd() {
		return ingListAtEnd;
	}

	/**
	 * <p>Setter for the field <code>ingListAtEnd</code>.</p>
	 *
	 * @param ingListAtEnd a {@link java.util.Map} object.
	 */
	public void setIngListAtEnd(Map<NodeRef, CompositeLabeling> ingListAtEnd) {
		this.ingListAtEnd = ingListAtEnd;
	}

	/**
	 * <p>Getter for the field <code>ingList</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<NodeRef, CompositeLabeling> getIngList() {
		return ingList;
	}

	/**
	 * <p>Setter for the field <code>ingList</code>.</p>
	 *
	 * @param ingList a {@link java.util.Map} object.
	 */
	public void setIngList(Map<NodeRef, CompositeLabeling> ingList) {
		this.ingList = ingList;
	}

	/** {@inheritDoc} */
	@Override
	public CompositeLabeling clone() {
		return new CompositeLabeling(this);
	}

	/** {@inheritDoc} */
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
				.append(" ( allergens:" + getAllergens() + ") ").append(" ( plural:" + isPlural() + ") ").append(" - ").append(getQty()).append(" (").append(getQtyTotal()).append(", vol: ")
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

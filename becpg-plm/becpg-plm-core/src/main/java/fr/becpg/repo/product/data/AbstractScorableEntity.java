package fr.becpg.repo.product.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>Abstract AbstractScorableEntity class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractScorableEntity extends BeCPGDataObject implements ScorableEntity {

	/**
	 *
	 */
	private static final long serialVersionUID = 3386870503758578033L;

	private List<ReqCtrlListDataItem> reqCtrlList;

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>reqCtrlList</code>.</p>
	 */
	@Override
	@DataList
	@AlfQname(qname = "bcpg:reqCtrlList")
	public List<ReqCtrlListDataItem> getReqCtrlList() {
		return reqCtrlList;
	}

	/**
	 * <p>Setter for the field <code>reqCtrlList</code>.</p>
	 *
	 * @param reqCtrlList a {@link java.util.List} object.
	 */
	public void setReqCtrlList(List<ReqCtrlListDataItem> reqCtrlList) {
		this.reqCtrlList = reqCtrlList;
	}

	// Formula helpers

	/** {@inheritDoc} */
	@Override
	public void addWarning(String msg) {
		addMessage(new MLText(msg), RequirementType.Tolerated);
	}

	/** {@inheritDoc} */
	@Override
	public void addError(String msg) {
		addMessage(new MLText(msg), RequirementType.Forbidden);
	}

	/** {@inheritDoc} */
	@Override
	public void addError(MLText msg) {
		addMessage(msg, RequirementType.Forbidden);
	}

	/** {@inheritDoc} */
	@Override
	public void addInfo(String msg) {
		addMessage(new MLText(msg), RequirementType.Info);
	}

	private void addMessage(MLText msg, RequirementType type) {
		reqCtrlList.add(ReqCtrlListDataItem.build().ofType(type).withMessage(msg).ofDataType(RequirementDataType.Formulation));

	}

	/** {@inheritDoc} */
	@Override
	public void addError(MLText msg, String formulationChainId, List<NodeRef> sources) {

		reqCtrlList.add(ReqCtrlListDataItem.forbidden().withMessage(msg)
				.ofDataType(RequirementDataType.Formulation).withFormulationChainId(formulationChainId));
	}

	/** {@inheritDoc} */
	@Override
	public boolean merge() {
		return merge(null);
	}

	/** {@inheritDoc} */
	@Override
	public boolean merge(List<String> disabledChainIds) {

		String currentChainId = getFormulationChainId();

		boolean hasChanged = false;

		if (reqCtrlList != null) {
			Map<String, ReqCtrlListDataItem> dbReqCtrlList = new HashMap<>();
			Map<String, ReqCtrlListDataItem> newReqCtrlList = new HashMap<>();
			List<ReqCtrlListDataItem> duplicates = new ArrayList<>();

			for (ReqCtrlListDataItem r : reqCtrlList) {
				if (r.getNodeRef() != null) {
					merge(dbReqCtrlList, r, duplicates);
				} else {
					merge(newReqCtrlList, r, duplicates);
				}
			}

			for (ReqCtrlListDataItem dup : duplicates) {
				reqCtrlList.remove(dup);
				hasChanged = true;
			}

			for (Entry<String, ReqCtrlListDataItem> entry : newReqCtrlList.entrySet()) {
				if (!dbReqCtrlList.keySet().contains(entry.getKey())) {
					hasChanged = true;
					break;
				}
			}

			for (Map.Entry<String, ReqCtrlListDataItem> dbKV : dbReqCtrlList.entrySet()) {
				if (!newReqCtrlList.containsKey(dbKV.getKey())) {

					if (((dbKV.getValue().getFormulationChainId() == null)
							&& ((getFormulationChainId() == null) || FormulationService.FAST_FORMULATION_CHAINID.equals(currentChainId)
									|| FormulationService.DEFAULT_CHAIN_ID.equals(currentChainId)))
							|| ((dbKV.getValue().getFormulationChainId() != null)
									&& dbKV.getValue().getFormulationChainId().equals(currentChainId))) {
						// remove
						reqCtrlList.remove(dbKV.getValue());
						hasChanged = true;
					}
				} else {

					// update
					ReqCtrlListDataItem newReqCtrlListDataItem = newReqCtrlList.get(dbKV.getKey());
					dbKV.getValue().setReqType(newReqCtrlListDataItem.getReqType());
					dbKV.getValue().setReqMaxQty(newReqCtrlListDataItem.getReqMaxQty());
					if (newReqCtrlListDataItem.getSources() != null) {
						dbKV.getValue().setSources(new ArrayList<>(newReqCtrlListDataItem.getSources()));
					}
					dbKV.getValue().setCharact(newReqCtrlListDataItem.getCharact());
					dbKV.getValue().setReqDataType(newReqCtrlListDataItem.getReqDataType());
					dbKV.getValue().setFormulationChainId(newReqCtrlListDataItem.getFormulationChainId());

					reqCtrlList.remove(newReqCtrlListDataItem);
				}
			}

			// sort
			sort(reqCtrlList);

			if (disabledChainIds != null) {

				List<ReqCtrlListDataItem> toRemove = new ArrayList<>();

				for (ReqCtrlListDataItem reqCtrl : reqCtrlList) {
					if (reqCtrl.getFormulationChainId() != null && disabledChainIds.contains(reqCtrl.getFormulationChainId())) {
						toRemove.add(reqCtrl);
					}
				}

				if (!toRemove.isEmpty()) {
					reqCtrlList.removeAll(toRemove);
					hasChanged = true;
				}
			}
		}

		return hasChanged;
	}

	private void merge(Map<String, ReqCtrlListDataItem> reqCtrlList, ReqCtrlListDataItem r, List<ReqCtrlListDataItem> duplicates) {
		if (reqCtrlList.containsKey(r.getKey())) {
			ReqCtrlListDataItem dbReq = reqCtrlList.get(r.getKey());

			duplicates.add(r);
			// Merge sources
			if (r.getSources() != null) {
				for (NodeRef tmpref : r.getSources()) {
					dbReq.addSource(tmpref);
				}
			}

		} else {

			reqCtrlList.put(r.getKey(), r);
		}

	}

	/**
	 * Sort by type
	 *
	 */
	private void sort(List<ReqCtrlListDataItem> reqCtrlList) {

		//Sort sources
		for (ReqCtrlListDataItem r : reqCtrlList) {
			if (r.getSources() != null) {
				r.getSources().sort(Comparator.comparing(NodeRef::getId));
			}
		}

		AtomicInteger index = new AtomicInteger();
		reqCtrlList.stream().sorted(Comparator.comparing(ReqCtrlListDataItem::getReqType, Comparator.nullsFirst(Comparator.naturalOrder())))
				.forEach(r -> r.setSort(index.getAndIncrement()));

	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(reqCtrlList);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractScorableEntity other = (AbstractScorableEntity) obj;
		return Objects.equals(reqCtrlList, other.reqCtrlList);
	}

}

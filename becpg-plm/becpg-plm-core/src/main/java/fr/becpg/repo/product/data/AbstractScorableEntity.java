package fr.becpg.repo.product.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BeCPGDataObject;

public abstract class AbstractScorableEntity extends BeCPGDataObject implements ScorableEntity {

	/**
	 *
	 */
	private static final long serialVersionUID = 3386870503758578033L;

	private List<ReqCtrlListDataItem> reqCtrlList;

	/**
	 * <p>Getter for the field <code>reqCtrlList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
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

	@Override
	public void addWarning(String msg) {
		addMessage(new MLText(msg), RequirementType.Tolerated);
	}

	@Override
	public void addError(String msg) {
		addMessage(new MLText(msg), RequirementType.Forbidden);
	}

	@Override
	public void addError(MLText msg) {
		addMessage(msg, RequirementType.Forbidden);
	}

	@Override
	public void addInfo(String msg) {
		addMessage(new MLText(msg), RequirementType.Info);
	}

	private void addMessage(MLText msg, RequirementType type) {
		reqCtrlList.add(new ReqCtrlListDataItem(null, type, msg, null, new ArrayList<>(), RequirementDataType.Formulation));
	}

	@Override
	public void addError(String msg, String formulationChainId, List<NodeRef> sources) {
		ReqCtrlListDataItem item = new ReqCtrlListDataItem(null, RequirementType.Forbidden, new MLText(msg), null, sources, null);
		item.setFormulationChainId(formulationChainId);
		reqCtrlList.add(item);
	}

	@Override
	public boolean merge() {

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
			}

			for (Entry<String, ReqCtrlListDataItem> entry : newReqCtrlList.entrySet()) {
				if (!dbReqCtrlList.keySet().contains(entry.getKey())) {
					hasChanged = true;
					break;
				}
			}

			for (Map.Entry<String, ReqCtrlListDataItem> dbKV : dbReqCtrlList.entrySet()) {
				if (!newReqCtrlList.containsKey(dbKV.getKey())) {

					if ((dbKV.getValue().getFormulationChainId() == null)
							|| dbKV.getValue().getFormulationChainId().equals(getFormulationChainId())) {
						// remove
						reqCtrlList.remove(dbKV.getValue());
						hasChanged = true;
					}
				} else {
					// update
					ReqCtrlListDataItem newReqCtrlListDataItem = newReqCtrlList.get(dbKV.getKey());

					if (((dbKV.getValue().getReqType() != null) || (newReqCtrlListDataItem.getReqType() != null))
							&& (dbKV.getValue().getReqType() != newReqCtrlListDataItem.getReqType())) {
						dbKV.getValue().setReqType(newReqCtrlListDataItem.getReqType());
						hasChanged = true;
					}
					if (((dbKV.getValue().getReqMaxQty() != null) && !dbKV.getValue().getReqMaxQty().equals(newReqCtrlListDataItem.getReqMaxQty()))
							|| ((newReqCtrlListDataItem.getReqMaxQty() != null)
									&& !newReqCtrlListDataItem.getReqMaxQty().equals(dbKV.getValue().getReqMaxQty()))) {
						dbKV.getValue().setReqMaxQty(newReqCtrlListDataItem.getReqMaxQty());
						hasChanged = true;
					}
					if (((dbKV.getValue().getSources() != null) && !dbKV.getValue().getSources().equals(newReqCtrlListDataItem.getSources()))
							|| ((newReqCtrlListDataItem.getSources() != null)
									&& !newReqCtrlListDataItem.getSources().equals(dbKV.getValue().getSources()))) {
						dbKV.getValue().setSources(newReqCtrlListDataItem.getSources());
						hasChanged = true;
					}
					if (((dbKV.getValue().getCharact() != null) && !dbKV.getValue().getCharact().equals(newReqCtrlListDataItem.getCharact()))
							|| ((newReqCtrlListDataItem.getCharact() != null)
									&& !newReqCtrlListDataItem.getCharact().equals(dbKV.getValue().getCharact()))) {
						dbKV.getValue().setCharact(newReqCtrlListDataItem.getCharact());
						hasChanged = true;
					}
					if (((dbKV.getValue().getReqDataType() != null) || (newReqCtrlListDataItem.getReqDataType() != null))
							&& (dbKV.getValue().getReqDataType() != newReqCtrlListDataItem.getReqDataType())) {
						dbKV.getValue().setReqDataType(newReqCtrlListDataItem.getReqDataType());
						hasChanged = true;
					}
					reqCtrlList.remove(newReqCtrlListDataItem);
				}
			}

			// sort
			sort(reqCtrlList);
		}

		return hasChanged;
	}

	private void merge(Map<String, ReqCtrlListDataItem> reqCtrlList, ReqCtrlListDataItem r, List<ReqCtrlListDataItem> duplicates) {
		if (reqCtrlList.containsKey(r.getKey())) {
			ReqCtrlListDataItem dbReq = reqCtrlList.get(r.getKey());

			duplicates.add(r);
			// Merge sources
			for (NodeRef tmpref : r.getSources()) {
				if (!dbReq.getSources().contains(tmpref)) {
					dbReq.getSources().add(tmpref);
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

		AtomicInteger index = new AtomicInteger();
		reqCtrlList.stream().sorted(Comparator.comparing(ReqCtrlListDataItem::getReqType, Comparator.nullsFirst(Comparator.naturalOrder())))
				.forEach(r -> r.setSort(index.getAndIncrement()));

	}

	

}

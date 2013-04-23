package fr.becpg.repo.data.hierarchicalList;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CompositeHelper {

	private static Log logger = LogFactory.getLog(CompositeHelper.class);

	public static <T extends CompositeDataItem> Composite<T> getHierarchicalCompoList(List<T> items) {

		Composite<T> composite = new Composite<T>();
		// loadChildren(composite, items);
		loadChildren(composite, 1, 0, items);
		if (logger.isDebugEnabled()) {
			logger.debug(composite.toString());
		}
		return composite;
	}

	// TODO Compute by parent instead
	@Deprecated
	private static <T extends CompositeDataItem> int loadChildren(Composite<T> composite, int level, int startPos, List<T> items) {

		int z_idx = startPos;

		for (; z_idx < items.size(); z_idx++) {

			T compoListDataItem = items.get(z_idx);

			if (compoListDataItem.getDepthLevel() == level) {

				// is composite ?
				boolean isComposite = false;
				if ((z_idx + 1) < items.size()) {

					T nextComponent = items.get(z_idx + 1);
					if (nextComponent.getDepthLevel() > compoListDataItem.getDepthLevel()) {
						isComposite = true;
					}
				}

				Composite<T> c = new Composite<T>(compoListDataItem);
				composite.addChild(c);

				if (isComposite) {

					z_idx = loadChildren(c, level + 1, z_idx + 1, items);
				}
			} else if (compoListDataItem.getDepthLevel() < level) {
				z_idx--;
				break;
			}
		}

		return z_idx;
	}

	// private static <T extends CompositeDataItem> void
	// loadChildren(Composite<T> composite, List<T> items ){
	// for (T item : items){
	// if(item.getParent() == composite.getData()){
	// Composite<T> temp = new Composite<T>(item);
	// composite.addChild(temp);
	// loadChildren(temp, items);
	// }
	//
	// }
	//
	// }

}

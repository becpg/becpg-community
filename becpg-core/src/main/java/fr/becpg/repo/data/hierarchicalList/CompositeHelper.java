package fr.becpg.repo.data.hierarchicalList;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CompositeHelper {

	private static Log logger = LogFactory.getLog(CompositeHelper.class);

	public static <T extends CompositeDataItem<T>> Composite<T> getHierarchicalCompoList(List<T> items) {

		Composite<T> composite = new Composite<T>();
		loadChildren(composite, items);
		if (logger.isDebugEnabled()) {
			logger.debug(composite.toString());
		}
		return composite;
	}

	 private static <T extends CompositeDataItem<T>> void loadChildren(Composite<T> composite, List<T> items ){
		 for (T item : items){
			 if((item.getParent() == null && composite.getData() == null) 
					 ||  (item.getParent() != null && item.getParent().equals(composite.getData()))){
				 Composite<T> temp = new Composite<T>(item);
				 composite.addChild(temp);
				 loadChildren(temp, items);
			 }
		 }
	 }

}

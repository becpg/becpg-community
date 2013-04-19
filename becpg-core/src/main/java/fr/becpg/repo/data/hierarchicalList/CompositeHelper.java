package fr.becpg.repo.data.hierarchicalList;

import java.util.List;

public class CompositeHelper {

	 	public static <T  extends CompositeDataItem>  Composite<T> getHierarchicalCompoList(List<T> items){
			
			Composite<T> composite = new Composite<T>();
			loadChildren(composite, 1, 0, items);
			return composite;
		}
		
		//TODO Compute by parent instead
		@Deprecated 
		private static <T  extends CompositeDataItem> int loadChildren(Composite<T> composite, int level, int startPos, List<T> items){
			
			int z_idx = startPos; 
			
			for( ; z_idx<items.size() ; z_idx++){
				
				T compoListDataItem = items.get(z_idx);
				
				if(compoListDataItem.getDepthLevel() == level){				
					
					// is composite ?
					boolean isComposite = false;
					if((z_idx+1) < items.size()){
					
						T nextComponent = items.get(z_idx+1);
						if(nextComponent.getDepthLevel() > compoListDataItem.getDepthLevel()){
							isComposite = true;
						}
					}
					
					if(isComposite){
						Composite<T> c = new Composite<T>(compoListDataItem);
						composite.addChild(c);
						z_idx = loadChildren(c, level+1, z_idx+1, items);
					}
					else{
						Leaf<T> leaf = new Leaf<T>(compoListDataItem);
						composite.addChild(leaf);
					}				
				}
				else if(compoListDataItem.getDepthLevel() < level){
					z_idx--;
					break;				
				}
			}
			
			return z_idx;
		}
		

	
}

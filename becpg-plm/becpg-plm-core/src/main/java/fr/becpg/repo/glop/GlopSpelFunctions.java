package fr.becpg.repo.glop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.spel.CustomSpelFunctions;
import fr.becpg.repo.glop.model.GlopConstraint;
import fr.becpg.repo.glop.model.GlopContext;
import fr.becpg.repo.glop.model.GlopData;
import fr.becpg.repo.glop.model.GlopTarget;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.impl.LazyLoadingDataList;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;

/**
 * Register custom Glop SPEL helper accessible with @glop.
 *
 * <h1>Usage</h1> In the composition list, add a dynamic formula making a call
 * to {@code @glop.optimize()}. The parameter should be an object of the following structure:
 * <ul>
 * <li>{@code target} is an object describing the target function and is
 * comprised of:
 * <ul>
 * <li>{@code var}, the data item to optimize</li>
 * <li>{@code task}, the optimization task, must be "min" or "max"
 * </ul>
 * </li>
 * <li>{@code constraints} is a set of constraints, each one being objects with
 * the following structure:
 * <ul>
 * <li>{@code var}, either the data item of the constraint or the name tag of a
 * special constraint (see below)</li>
 * <li>{@code min}, the smallest accepted value for the constraint</li>
 * <li>{@code max}, the biggest accepted value for the constraint</li>
 * </ul>
 * </li>
 * </ul>
 *
 * The {@code min} and {@code max} values may be floating-point numbers or the
 * strings {@code "inf"} and {@code "-inf"}.
 *
 * <p>
 * The result is an object with the following structure:
 * <ul>
 * <li>{@code coefficients}: a map where the keys are the component names and
 * the values are their quantity in the optimized recipe</li>
 * <li>{@code value}: the value that the target function takes under this
 * calculated recipe</li>
 * <li>{@code status}: "optimized" if the solution is optimal, "feasible" if it
 * satisfies the constraints but isn't guaranteed optimal</li>
 * </ul>
 *
 * <h1>Types of constraint</h1> Data constraints are calculated from instances
 * of sub-classes of {@link fr.becpg.repo.repository.model.BeCPGDataObject} that
 * are referenced in the product data. The types currently supported are:
 * <ul>
 * <li>{@link fr.becpg.repo.product.data.productList.CompoListDataItem}, through
 * the SpEL expression {@code compo["ref"]}</li>
 * <li>{@link fr.becpg.repo.product.data.productList.NutListDataItem}, through
 * the SpEL expression {@code nut["ref"]}</li>
 * <li>{@link fr.becpg.repo.product.data.productList.CostListDataItem}, through
 * the SpEL expression {@code cost["ref"]}</li>
 * <li>{@link fr.becpg.repo.product.data.productList.IngListDataItem}, through
 * the SpEL expression {@code ing["ref"]}</li>
 * <li>{@link fr.becpg.repo.product.data.productList.AllergenListDataItem},
 * through the SpEL expression {@code allergen["ref"]}</li>
 * <li>{@link fr.becpg.repo.product.data.productList.PackagingListDataItem},
 * through the SpEL expression {@code packaging["ref"]}</li>
 * </ul>
 *
 * Special constraints are constraints that require more than one data item to
 * create. Currently, the only special constraint is {@code "recipeQtyUsed"} which
 * evaluates as the total quantity of components in the product.
 *
 * @author pierrecolin
 * @see fr.becpg.repo.glop.GlopService
 * @version $Id: $Id
 */
@Service
public class GlopSpelFunctions implements CustomSpelFunctions {

	@Autowired
	private GlopService glopService;
	
	@Autowired
	private NodeService nodeService;
	
	/** {@inheritDoc} */
	@Override
	public boolean match(String beanName) {
		return beanName.equals("glop");
	}

	/** {@inheritDoc} */
	@Override
	public Object create(RepositoryEntity repositoryEntity) {
		return new GlopSpelFunctionsWrapper(repositoryEntity);
	}

	public class GlopSpelFunctionsWrapper {

		private static final String RECIPE_QTY_USED = "recipeQtyUsed";

		RepositoryEntity entity;

		public GlopSpelFunctionsWrapper(RepositoryEntity entity) {
			super();
			this.entity = entity;
		}

		public GlopData optimize(Map<String, ?> problem) {
			return glopService.optimize((ProductData) entity, buildGlopContext(problem));
		}

		private GlopContext buildGlopContext(Map<String, ?> problem) throws IllegalArgumentException {
			GlopContext glopContext = new GlopContext();
			
			glopContext.setTotalQuantity(getTotalQuantity((ProductData) entity, (Collection<?>) problem.get("constraints")));
			
			glopContext.setTarget(buildGlopTarget(problem));
			
			glopContext.setConstraints(buildGlopConstraints(problem, glopContext.getTotalQuantity()));
			
			return glopContext;
		}
		
		@SuppressWarnings("unchecked")
		private GlopTarget buildGlopTarget(Map<String, ?> problem) throws IllegalArgumentException {
			Object ret = problem.get("target");
			
			Map<String, ?> targetMap = null;
			
			if (ret instanceof Map<?, ?>) {
				targetMap = (Map<String, ?>) ret;
			} else if (ret instanceof Collection<?>) {
				Collection<?> c = (Collection<?>) ret;
				if (c.size() == 1) {
					targetMap = (Map<String, ?>) c.iterator().next();
				} else {
					throw new IllegalArgumentException("target list must have size 1");
				}
			} else {
				throw new IllegalArgumentException("target specification is not a map");
			}
			
			SimpleCharactDataItem targetItem = (SimpleCharactDataItem) targetMap.get("var");
			String targetTask = (String) targetMap.get("task");
			return new GlopTarget(targetItem, targetTask);
			
		}

		@SuppressWarnings("unchecked")
		private List<GlopConstraint> buildGlopConstraints(Map<String, ?> problem, Double totalQuantity) {
			Object objConstraints = problem.get("constraints");
			if (!(objConstraints instanceof Collection<?>)) {
				throw new IllegalArgumentException("constraints must be a collection");
			}
			
			boolean recipeQtyUsedFound = false;
			
			List<GlopConstraint> constraints = new ArrayList<>();
			
			for (Object objConstraint : (Collection<?>) objConstraints) {
				
				Map<String, ?> constraint = (Map<String, ?>) objConstraint;
				
				if (constraint.containsKey("list")) {
					Object objConstraintItem = constraint.get("list");
					if (objConstraintItem instanceof LazyLoadingDataList) {
						
						LazyLoadingDataList<RepositoryEntity> list = (LazyLoadingDataList<RepositoryEntity>) objConstraintItem;
						
						for (RepositoryEntity item : list) {
							
							if (item instanceof SimpleCharactDataItem) {
								nodeService.removeProperty(item.getNodeRef(), PLMModel.PROP_GLOP_VALUE);
								String glopTarget = (String) nodeService.getProperty(item.getNodeRef(), PLMModel.PROP_GLOP_TARGET);
								if (glopTarget != null && !glopTarget.isBlank()) {
									
									Double tolerance = null;
									
									glopTarget = glopTarget.replace(" ", "");
									
									if (glopTarget.contains("(") && glopTarget.contains(")")) {
										tolerance = Double.parseDouble(glopTarget.split("\\(")[1].replace(")", ""));
										
										glopTarget = glopTarget.replace(glopTarget.split("\\(")[1], "").replace("(", "");
									}
									
									String[] split = glopTarget.split(";");
									
									Double minValue = Double.parseDouble(split[0]) * (item instanceof SimpleListDataItem ? totalQuantity : 1d);
									Double maxValue = split.length < 2 ? minValue : Double.parseDouble(split[1]) * (item instanceof SimpleListDataItem ? totalQuantity : 1d);
									
									GlopConstraint glopConstraint = new GlopConstraint(item, minValue, maxValue);
									
									if (tolerance == null && constraint.containsKey("tol")) {
										tolerance = buildDouble(constraint, "tol");
									}
									
									glopConstraint.setTolerance(tolerance);
									constraints.add(glopConstraint);
								}
							}
						}
						
					}
				} else if (constraint.containsKey("var")) {
					Object constraintItem = constraint.get("var");
					
					GlopConstraint glopConstraint = null;
					
					if (RECIPE_QTY_USED.equals(constraintItem)) {
						recipeQtyUsedFound = true;
					}
					
					Double constraintMin = buildDouble(constraint,"min");
					Double constraintMax = buildDouble(constraint,"max");
					
					glopConstraint = new GlopConstraint(constraintItem, constraintMin, constraintMax);
					
					if (constraint.containsKey("tol")) {
						glopConstraint.setTolerance(buildDouble(constraint,"tol"));
					}
					
					constraints.add(glopConstraint);
					
				}
				
			}
			
			if (!recipeQtyUsedFound) {
				constraints.add(new GlopConstraint(RECIPE_QTY_USED, ((ProductData) entity).getRecipeQtyUsed(), ((ProductData) entity).getRecipeQtyUsed()));
			}
			
			return constraints;
		}

		private double buildDouble(Map<String, ?> constraint, String key) {
			
			Object obj = constraint.get(key);
			
			if (obj == null) {
				Object constraintName = constraint.get("var");
				
				throw new IllegalArgumentException("Constraint '" + constraintName + "' has no '" + key + "' value");
			}
			
			if (obj instanceof Double) {
				return (Double) obj;
			} else if (obj instanceof Integer) {
				return (Integer) obj;
			} else if (obj instanceof String) {
				String str = (String) obj;
				if (str.equals("inf")) {
					return Double.POSITIVE_INFINITY;
				} else if (str.equals("-inf")) {
					return Double.NEGATIVE_INFINITY;
				}
			}
			
			Object constraintName = constraint.containsKey("var") ? constraint.get("var") : "list";

			throw new IllegalArgumentException("Constraint '" + constraintName + "' has no double value for '" + key);
		}
		
		private Double getTotalQuantity(ProductData entity, Collection<?> objConstraints) throws IllegalArgumentException {
			if (entity.getNetWeight() != null && entity.getNetWeight() != 0d) {
				return entity.getNetWeight();
			}
			
			for (Object objConstraint : objConstraints) {
				
				@SuppressWarnings("unchecked")
				Map<String, ?> constraint = (Map<String, ?>) objConstraint;
				
				if (constraint.containsKey("var") && RECIPE_QTY_USED.equals(constraint.get("var"))) {
					
					
					Double constraintMin = buildDouble(constraint,"min");
					
					Double constraintMax = buildDouble(constraint,"max");
					
					if (!constraintMin.equals(constraintMax)) {
						throw new IllegalArgumentException(RECIPE_QTY_USED + " constraint must have same 'min' and 'max' values");
					}

					return constraintMin;
				}
			}
			
			if (entity.getRecipeQtyUsed() != null && entity.getRecipeQtyUsed() != 0d) {
				return entity.getRecipeQtyUsed();
			}
			
			return 1d;
			
		}

	}

}

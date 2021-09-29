package fr.becpg.repo.glop;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import fr.becpg.repo.formulation.spel.CustomSpelFunctions;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

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
	private AttributeExtractorService attributeExtractorService;

	@Autowired
	private GlopService glopService;
	
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

		private static final String COMPONENTS = "components";
		private static final String STATUS = "status";
		private static final String VALUE = "value";
		private static final String NAME = "name";
		private static final String ID = "id";
		
		RepositoryEntity entity;

		public GlopSpelFunctionsWrapper(RepositoryEntity entity) {
			super();
			this.entity = entity;
		}

		@SuppressWarnings("unchecked")
		private Map<String, ?> getTarget(Map<String, ?> problem) throws IllegalArgumentException {
			Object ret = problem.get("target");
			if (ret instanceof Map<?, ?>) {
				return (Map<String, ?>) ret;
			} else if (ret instanceof Collection<?>) {
				Collection<?> c = (Collection<?>) ret;
				if (c.size() == 1) {
					return (Map<String, ?>) c.iterator().next();
				} else {
					throw new IllegalArgumentException("target list must have size 1");
				}
			} else {
				throw new IllegalArgumentException("target specification is not a map");
			}
		}

		private double getDouble(Object obj) {
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
			throw new IllegalArgumentException("Expected Double, got " + obj.getClass().getName());
		}

		private String translate(JSONObject obj) throws JSONException {
			
			JSONObject ret = new JSONObject();
			
			JSONArray components = new JSONArray();
			
			JSONObject coeff = (JSONObject) obj.get("coefficients");
			
			Iterator<String> keys = coeff.keys();

			while (keys.hasNext()) {
				String key = keys.next();
				
				JSONObject component = new JSONObject();
				
				NodeRef nodeRef = new NodeRef(key);
				
				component.put(ID, nodeRef.toString());
				component.put(NAME, attributeExtractorService.extractPropName(nodeRef));
				component.put(VALUE, coeff.getDouble(key));
				
				components.put(component);
			}
			
			ret.put(COMPONENTS, components);
			ret.put(VALUE, obj.get(VALUE));
			ret.put(STATUS, obj.get(STATUS));

			return ret.toString();
		}

		public Double extractValue(NodeRef nodeRef, String in) throws JSONException {
			
			JSONObject json = new JSONObject(in);
			
			JSONArray comps = json.getJSONArray(COMPONENTS);
			
			for (int index = 0; index < comps.length(); index++) {
				JSONObject comp = (JSONObject) comps.get(index);
				if (comp.has(ID) && comp.getString(ID).equals(nodeRef.toString())) {
					return comp.getDouble(VALUE);
				}
			}
			
			return null;
		}
		
		@SuppressWarnings("unchecked")
		public String optimize(Map<String, ?> problem) {
			Map<String, ?> target = getTarget(problem);
			SimpleCharactDataItem targetItem = (SimpleCharactDataItem) target.get("var");
			String targetTask = (String) target.get("task");
			GlopTargetSpecification fullTarget = new GlopTargetSpecification(targetItem, targetTask);

			Object objConstraints = problem.get("constraints");
			if (!(objConstraints instanceof Collection<?>)) {
				return "Error : constraints must be a collection";
			}
			Collection<?> constraints = (Collection<?>) objConstraints;
			List<GlopConstraintSpecification> fullConstraints = new ArrayList<>();
			for (Object objConstraint : constraints) {

				Map<String, ?> constraint = (Map<String, ?>) objConstraint;
				Double constraintMin = getDouble(constraint.get("min"));
				Double constraintMax = getDouble(constraint.get("max"));
				Object objConstraintItem = constraint.get("var");
				if (objConstraintItem instanceof SimpleCharactDataItem) {
					SimpleCharactDataItem constraintItem = (SimpleCharactDataItem) objConstraintItem;
					fullConstraints.add(new GlopConstraintSpecification(constraintItem, constraintMin, constraintMax));
				} else {
					String constraintType = (String) objConstraintItem;
					fullConstraints.add(new GlopConstraintSpecification(constraintType, constraintMin, constraintMax));
				}
			}
			try {
				JSONObject response = glopService.optimize((ProductData) entity, fullConstraints, fullTarget);
				return translate(response);
			} catch (GlopException e) {
				return "Error : Linear program is unfeasible: " + e;
			} catch (JSONException e) {
				return "Error : Failed to build request to send to the Glop server: " + e;
			} catch (URISyntaxException e) {
				return "Error : Glop server URI has a syntax error: " + e;
			} catch (RestClientException e) {
				return "Error : Failed to send request to the Glop server: " + e;
			}
		}
	}

}

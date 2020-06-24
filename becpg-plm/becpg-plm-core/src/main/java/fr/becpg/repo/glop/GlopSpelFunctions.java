package fr.becpg.repo.glop;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
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
 * to {@code @glop.optimize()}. The first parameter should be {@code #this}. The
 * second should be an object of the following structure:
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
 * create. Currently, the only special constraint is {@code "total_qty"} which
 * evaluates as the total quantity of components in the product.
 *
 * @author pierrecolin
 * @see fr.becpg.repo.glop.GlopService
 */
@Service
public class GlopSpelFunctions implements CustomSpelFunctions {

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	@Autowired
	private GlopService glopService;

	@Override
	public boolean match(String beanName) {
		return beanName.equals("glop");
	}

	@Override
	public Object create(RepositoryEntity repositoryEntity) {
		return new GlopSpelFunctionsWrapper(repositoryEntity);
	}

	public class GlopSpelFunctionsWrapper {

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

		private Map<String, Serializable> translate(JSONObject obj) throws JSONException {
			Map<String, Serializable> res = new HashMap<>();
			res.put("status", obj.getString("status"));
			res.put("value", obj.getDouble("value"));
			org.json.JSONObject jsonCoefficients = obj.getJSONObject("coefficients");
			Map<String, Double> translatedCoefficients = new HashMap<>();
			for (String ref : org.json.JSONObject.getNames(jsonCoefficients)) {
				NodeRef node = new NodeRef(ref);
				translatedCoefficients.put(attributeExtractorService.extractPropName(node), jsonCoefficients.getDouble(ref));
			}
			res.put("coefficients", (Serializable) translatedCoefficients);
			return res;
		}

		@SuppressWarnings("unchecked")
		public Map<String, Serializable> optimize(ProductData product, Map<String, ?> problem) {
			Map<String, ?> target = getTarget(problem);
			SimpleCharactDataItem targetItem = (SimpleCharactDataItem) target.get("var");
			String targetTask = (String) target.get("task");
			GlopTargetSpecification fullTarget = new GlopTargetSpecification(targetItem, targetTask);

			Object objConstraints = problem.get("constraints");
			if (!(objConstraints instanceof Collection<?>)) {
				throw new IllegalArgumentException("constraints must be a collection");
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
				JSONObject response = glopService.optimize(product, fullConstraints, fullTarget);
				return translate(response);
			} catch (GlopException e) {
				throw new RuntimeException("Linear program is unfeasible", e);
			} catch (JSONException e) {
				throw new RuntimeException("Failed to build request to send to the Glop server", e);
			} catch (URISyntaxException e) {
				throw new RuntimeException("Glop server URI has a syntax error", e);
			} catch (RestClientException e) {
				throw new RuntimeException("Failed to send request to the Glop server: " + e.getMessage(), e);
			}
		}
	}

}

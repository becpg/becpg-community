package fr.becpg.repo.web.scripts.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.workflow.TaskInstancesGet;
import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery.OrderBy;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ModelUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.config.format.FormatMode;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.impl.AttributeExtractorField;

/**
 * <p>BeCPGTaskInstancesGet class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BeCPGTaskInstancesGet extends TaskInstancesGet {
	private static final Log LOGGER = LogFactory.getLog(BeCPGTaskInstancesGet.class);

	/** Constant <code>PARAM_SEARCH="q"</code> */
	public static final String PARAM_SEARCH = "q";
	/** Constant <code>PARAM_SORT="sort"</code> */
	public static final String PARAM_SORT = "sort";
	/** Constant <code>PARAM_DIR="dir"</code> */
	public static final String PARAM_DIR = "dir";
	private static final QName QNAME_INITIATOR = QName.createQName("wf", "initiator");

	private AttributeExtractorService attributeExtractorService;

	/**
	 * <p>Setter for the field <code>attributeExtractorService</code>.</p>
	 *
	 * @param attributeExtractorService a {@link fr.becpg.repo.helper.AttributeExtractorService} object.
	 */
	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}

	/** {@inheritDoc} */
	@Override
	protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status, Cache cache) {
		Map<String, String> params = req.getServiceMatch().getTemplateVars();
		Map<String, Object> filters = new HashMap<>(4);

		// authority is not included into filters list as it will be taken into
		// account before filtering
		String authority = getAuthority(req);

		if (authority == null) {
			// ALF-11036 fix, if authority argument is omitted the tasks for the
			// current user should be returned.
			authority = authenticationService.getCurrentUserName();
		}

		// state is also not included into filters list, for the same reason
		WorkflowTaskState state = getState(req);

		// look for a workflow instance id
		String workflowInstanceId = params.get(VAR_WORKFLOW_INSTANCE_ID);

		// determine if pooledTasks should be included, when appropriate i.e.
		// when an authority is supplied
		Boolean pooledTasksOnly = getPooledTasks(req);

		// get list of properties to include in the response
		List<String> tmp = getProperties(req);
		List<String> properties = new LinkedList<>();
		List<AttributeExtractorField> extraProperties = new LinkedList<>();

		for (String string : tmp) {
			String prop = string;
			if (prop.startsWith("extra_")) {
				extraProperties.add(new AttributeExtractorField(prop.replace("extra_", "").replace("_", ":"),null));
			} else {
				properties.add(prop);
			}
		}

		// get filter param values
		filters.put(PARAM_PRIORITY, req.getParameter(PARAM_PRIORITY));
		filters.put(PARAM_PROPERTY, req.getParameter(PARAM_PROPERTY));
		filters.put(PARAM_SEARCH, req.getParameter(PARAM_SEARCH));
		processDateFilter(req, PARAM_DUE_BEFORE, filters);
		processDateFilter(req, PARAM_DUE_AFTER, filters);

		String excludeParam = req.getParameter(PARAM_EXCLUDE);
		if ((excludeParam != null) && (excludeParam.length() > 0)) {
			filters.put(PARAM_EXCLUDE, new ExcludeFilter(excludeParam));
		}

		List<WorkflowTask> allTasks;

		if (workflowInstanceId != null) {
			// a workflow instance id was provided so query for tasks
			WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
			taskQuery.setActive(null);
			taskQuery.setProcessId(workflowInstanceId);
			taskQuery.setTaskState(state);
			taskQuery.setOrderBy(new OrderBy[] { OrderBy.TaskCreated_Asc});

			if (authority != null) {
				taskQuery.setActorId(authority);
			}

			allTasks = workflowService.queryTasks(taskQuery, false);
		} else {
			// default task state to IN_PROGRESS if not supplied
			if (state == null) {
				state = WorkflowTaskState.IN_PROGRESS;
			}

			// no workflow instance id is present so get all tasks
			if (authority != null) {
				List<WorkflowTask> tasks = workflowService.getAssignedTasks(authority, state, true);
				List<WorkflowTask> pooledTasks = workflowService.getPooledTasks(authority, true);
				if (pooledTasksOnly != null) {
					if (pooledTasksOnly.booleanValue()) {
						// only return pooled tasks the user can claim
						allTasks = new ArrayList<>(pooledTasks.size());
						allTasks.addAll(pooledTasks);
					} else {
						// only return tasks assigned to the user
						allTasks = new ArrayList<>(tasks.size());
						allTasks.addAll(tasks);
					}
				} else {
					// include both assigned and unassigned tasks
					allTasks = new ArrayList<>(tasks.size() + pooledTasks.size());
					allTasks.addAll(tasks);
					allTasks.addAll(pooledTasks);
				}

				String dir = req.getParameter(PARAM_DIR);

				QName sortProp = extractSort(req.getParameter(PARAM_SORT));

				boolean orderAsc = (dir == null) || dir.equalsIgnoreCase("asc");

				Comparator<WorkflowTask> taskComparator =  new WorkflowDateComparator(WorkflowModel.PROP_START_DATE, false);
				if (sortProp != null) {
					if (QNAME_INITIATOR.equals(sortProp)) {
						taskComparator = new WorkflowInitiatorComparator(orderAsc);
					} else {

						DataTypeDefinition type = dictionaryService.getProperty(sortProp).getDataType();
						if (String.class.getName().equals(type.getJavaClassName())) {
							taskComparator = new WorkflowStringComparator(sortProp, orderAsc);
						} else if (Date.class.getName().equals(type.getJavaClassName())) {
							taskComparator = new WorkflowDateComparator(sortProp, orderAsc);
						} else {
							taskComparator = new WorkflowNumberComparator(sortProp, orderAsc);
						}
					}
				}

				// sort tasks by due date
				Collections.sort(allTasks, taskComparator);
			} else {
				// authority was not provided -> return all active tasks in the
				// system
				WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
				taskQuery.setTaskState(state);
				taskQuery.setActive(null);
				taskQuery.setOrderBy(new OrderBy[] { OrderBy.TaskCreated_Asc });
				allTasks = workflowService.queryTasks(taskQuery, false);
			}
		}

		int maxItems = getIntParameter(req, PARAM_MAX_ITEMS, DEFAULT_MAX_ITEMS);
		int skipCount = getIntParameter(req, PARAM_SKIP_COUNT, DEFAULT_SKIP_COUNT);
		int totalCount = 0;
		ArrayList<Map<String, Object>> results = new ArrayList<>();

		// Filter results
		for (WorkflowTask task : allTasks) {
			if (matches(task, filters)) {
				// Total-count needs to be based on matching tasks only, so we
				// can't just use allTasks.size() for this
				totalCount++;
				if ((totalCount > skipCount) && ((maxItems < 0) || (maxItems > results.size()))) {
					// Only build the actual detail if it's in the range of
					// items we need. This will
					// drastically improve performance over paging after
					// building the model
					results.add(buildbeCPGModel(modelBuilder, task, properties, extraProperties));
				}
			}
		}

		Map<String, Object> model = new HashMap<>();
		model.put("taskInstances", results);

		if ((maxItems != DEFAULT_MAX_ITEMS) || (skipCount != DEFAULT_SKIP_COUNT)) {
			// maxItems or skipCount parameter was provided so we need to
			// include paging into response
			model.put("paging", ModelUtil.buildPaging(totalCount, maxItems == DEFAULT_MAX_ITEMS ? totalCount : maxItems, skipCount));
		}

		// create and return results, paginated if necessary
		return model;
	}

	private Map<String, Object> buildbeCPGModel(WorkflowModelBuilder modelBuilder, WorkflowTask task, List<String> properties,
			List<AttributeExtractorField> extraProperties) {
		Map<String, Object> ret = modelBuilder.buildSimple(task, properties);

		if (!extraProperties.isEmpty()) {

			NodeRef entityNodeRef = (NodeRef) task.getProperties().get(BeCPGModel.ASSOC_WORKFLOW_ENTITY);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Add extra properties for :" + entityNodeRef);
			}

			if ((entityNodeRef != null) && nodeService.exists(entityNodeRef)) {

				JSONObject tmp = new JSONObject(attributeExtractorService.extractNodeData(entityNodeRef, nodeService.getType(entityNodeRef),
						extraProperties, FormatMode.JSON));

				ret.put("extra", tmp.toString());

			}
		}

		return ret;
	}

	private QName extractSort(String parameter) {

		if (parameter != null) {
			switch (parameter) {
			case "priority":
				return WorkflowModel.PROP_PRIORITY;
			case "description":
				return WorkflowModel.PROP_DESCRIPTION;
			case "startDate":
				return WorkflowModel.PROP_START_DATE;
			case "dueDate":
				return WorkflowModel.PROP_DUE_DATE;
			case "completionDate":
				return WorkflowModel.PROP_COMPLETION_DATE;
			case "owner":
				return QNAME_INITIATOR;
			default:
				break;
			}
		}

		return null;
	}

	/**
	 * Retrieves the list of property names to include in the response.
	 *
	 * @param req
	 *            The WebScript request
	 * @return List of property names
	 */
	private List<String> getProperties(WebScriptRequest req) {
		String propertiesStr = req.getParameter(PARAM_PROPERTIES);
		if (propertiesStr != null) {
			return Arrays.asList(propertiesStr.split(","));
		}
		return Collections.emptyList();
	}

	/**
	 * Retrieves the pooledTasks parameter.
	 *
	 * @param req
	 *            The WebScript request
	 * @return null if not present, Boolean object otherwise
	 */
	private Boolean getPooledTasks(WebScriptRequest req) {
		Boolean result = null;
		String includePooledTasks = req.getParameter(PARAM_POOLED_TASKS);

		if (includePooledTasks != null) {
			result = Boolean.valueOf(includePooledTasks);
		}

		return result;
	}

	/**
	 * Gets the specified {@link WorkflowTaskState}, null if not requested
	 *
	 * @param req
	 *            WebScriptRequest
	 * @return WorkflowTaskState
	 */
	private WorkflowTaskState getState(WebScriptRequest req) {
		String stateName = req.getParameter(PARAM_STATE);
		if (stateName != null) {
			try {
				return WorkflowTaskState.valueOf(stateName.toUpperCase());
			} catch (IllegalArgumentException e) {
				String msg = "Unrecognised State parameter: " + stateName;
				throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
			}
		}

		return null;
	}

	/**
	 * Returns the specified authority. If no authority is specified then
	 * returns the current Fully Authenticated user.
	 *
	 * @param req
	 *            WebScriptRequest
	 * @return String
	 */
	private String getAuthority(WebScriptRequest req) {
		String authority = req.getParameter(PARAM_AUTHORITY);
		if ((authority == null) || (authority.length() == 0)) {
			authority = null;
		}
		return authority;
	}

	/**
	 * Determine if the given task should be included in the response.
	 *
	 * @param task
	 *            The task to check
	 * @param filters
	 *            The list of filters the task must match to be included
	 * @return true if the task matches and should therefore be returned
	 */
	private boolean matches(WorkflowTask task, Map<String, Object> filters) {
		// by default we assume that workflow task should be included
		boolean result = true;

		for (String key : filters.keySet()) {
			Object filterValue = filters.get(key);

			// skip null filters (null value means that filter was not
			// specified)
			if (filterValue != null) {
				if (key.equals(PARAM_EXCLUDE)) {
					ExcludeFilter excludeFilter = (ExcludeFilter) filterValue;
					String type = task.getDefinition().getMetadata().getName().toPrefixString(this.namespaceService);
					if (excludeFilter.isMatch(type)) {
						result = false;
						break;
					}
				} else if (key.equals(PARAM_DUE_BEFORE)) {
					Date dueDate = (Date) task.getProperties().get(WorkflowModel.PROP_DUE_DATE);

					if (!isDateMatchForFilter(dueDate, filterValue, true)) {
						result = false;
						break;
					}
				} else if (key.equals(PARAM_DUE_AFTER)) {
					Date dueDate = (Date) task.getProperties().get(WorkflowModel.PROP_DUE_DATE);

					if (!isDateMatchForFilter(dueDate, filterValue, false)) {
						result = false;
						break;
					}
				} else if (key.equals(PARAM_PRIORITY)) {
					if (!filterValue.equals(task.getProperties().get(WorkflowModel.PROP_PRIORITY).toString())) {
						result = false;
						break;
					}
				} else if (key.equals(PARAM_PROPERTY)) {
					int propQNameEnd = filterValue.toString().indexOf('/');
					if (propQNameEnd < 1) {
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Ignoring invalid property filter:" + filterValue.toString());
						}
						break;
					}
					String propValue = filterValue.toString().substring(propQNameEnd + 1);
					if (propValue.isEmpty()) {
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Ignoring empty property value filter [" + propValue + "]");
						}
						break;
					}
					String propQNameStr = filterValue.toString().substring(0, propQNameEnd);
					QName propertyQName;
					try {
						propertyQName = QName.createQName(propQNameStr, namespaceService);
					} catch (Exception ex) {
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Ignoring invalid QName property filter [" + propQNameStr + "]");
						}
						break;
					}
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Filtering with property [" + propertyQName.toPrefixString(namespaceService) + "=" + propValue + "]");
					}
					Serializable value = task.getProperties().get(propertyQName);
					if ((value != null) && !value.equals(propValue)) {
						result = false;
						break;
					}
				} else if (key.equals(PARAM_SEARCH)) {
					String value = (String) task.getProperties().get(WorkflowModel.PROP_DESCRIPTION);
					if ((value != null) && !value.toLowerCase().contains(((String) filterValue).toLowerCase())) {
						result = false;
						break;
					}
				}
			}
		}

		return result;
	}

//	/**
//	 * Comparator to sort workflow tasks by due date in ascending order.
//	 */
//	class WorkflowTaskDueAscComparator implements Comparator<WorkflowTask> {
//		@Override
//		public int compare(WorkflowTask o1, WorkflowTask o2) {
//			Date date1 = (Date) o1.getProperties().get(WorkflowModel.PROP_DUE_DATE);
//			Date date2 = (Date) o2.getProperties().get(WorkflowModel.PROP_DUE_DATE);
//
//			long time1 = date1 == null ? Long.MAX_VALUE : date1.getTime();
//			long time2 = date2 == null ? Long.MAX_VALUE : date2.getTime();
//
//			long result = time1 - time2;
//
//			return (result > 0) ? 1 : (result < 0 ? -1 : 0);
//		}
//
//	}

	private class WorkflowDateComparator implements Comparator<WorkflowTask> {

		private QName property;
		private boolean orderAsc;

		public WorkflowDateComparator(QName property, boolean orderAsc) {

			this.property = property;
			this.orderAsc = orderAsc;
		}

		@Override
		public int compare(WorkflowTask o1, WorkflowTask o2) {

			Date date1 = (Date) o1.getProperties().get(property);
			Date date2 = (Date) o2.getProperties().get(property);

			long time1 = date1 == null ? Long.MAX_VALUE : date1.getTime();
			long time2 = date2 == null ? Long.MAX_VALUE : date2.getTime();

			long result = time1 - time2;
			int ret = (result > 0) ? 1 : (result < 0 ? -1 : 0);
			return orderAsc ? ret : ret * -1;
		}
	}

	private class WorkflowNumberComparator implements Comparator<WorkflowTask> {

		private QName property;
		private boolean orderAsc;

		public WorkflowNumberComparator(QName property, boolean orderAsc) {

			this.property = property;
			this.orderAsc = orderAsc;
		}

		@Override
		public int compare(WorkflowTask o1, WorkflowTask o2) {

			Number n1 = (Number) o1.getProperties().get(property);
			Number n2 = (Number) o2.getProperties().get(property);

			n1 = n1 == null ? Double.MIN_VALUE : n1;
			n2 = n2 == null ? Double.MIN_VALUE : n2;

			double result = n1.doubleValue() - n2.doubleValue();
			int ret = (result > 0) ? 1 : (result < 0 ? -1 : 0);
			return orderAsc ? ret : ret * -1;
		}
	}

	private class WorkflowStringComparator implements Comparator<WorkflowTask> {

		private QName property;
		private boolean orderAsc;

		public WorkflowStringComparator(QName property, boolean orderAsc) {

			this.property = property;
			this.orderAsc = orderAsc;
		}

		@Override
		public int compare(WorkflowTask o1, WorkflowTask o2) {

			String s1 = (String) o1.getProperties().get(property);
			String s2 = (String) o2.getProperties().get(property);

			s1 = s1 == null ? "" : s1;
			s2 = s2 == null ? "" : s2;

			int ret = s1.compareTo(s2);
			return orderAsc ? ret : ret * -1;
		}

	}

	private class WorkflowInitiatorComparator implements Comparator<WorkflowTask> {

		private boolean orderAsc;

		public WorkflowInitiatorComparator(boolean orderAsc) {

			this.orderAsc = orderAsc;
		}

		@Override
		public int compare(WorkflowTask o1, WorkflowTask o2) {

			NodeRef n1 = o1.getPath().getInstance().getInitiator();
			NodeRef n2 = o2.getPath().getInstance().getInitiator();
			String s1 = null, s2 = null;

			if (n1 != null) {
				s1 = nodeService.getProperty(n1, ContentModel.PROP_FIRSTNAME) + " " + nodeService.getProperty(n1, ContentModel.PROP_LASTNAME);
			}
			if (n2 != null) {
				s2 = nodeService.getProperty(n2, ContentModel.PROP_FIRSTNAME) + " " + nodeService.getProperty(n1, ContentModel.PROP_LASTNAME);
			}

			s1 = s1 == null ? "" : s1;
			s2 = s2 == null ? "" : s2;

			int ret = s1.compareTo(s2);
			return orderAsc ? ret : ret * -1;
		}

	}

}

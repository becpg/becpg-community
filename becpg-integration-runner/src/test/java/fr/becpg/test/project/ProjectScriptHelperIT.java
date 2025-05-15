package fr.becpg.test.project;

import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.URLEncoder;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.jscript.ProjectScriptHelper;
import fr.becpg.test.RepoBaseTestCase;

public class ProjectScriptHelperIT extends RepoBaseTestCase {

	@Autowired
	ServiceRegistry serviceRegistry;

	@Autowired
	ProjectScriptHelper projectScriptHelper;
	
	@Autowired
	EntityService entityService;

	@Test
	public void testCreateDeliverableUrl() {

		ProjectData ret = (ProjectData) inWriteTx(() -> {
			ProjectData projectData = ProjectData.build().withName("Deliverable test")
					.withTaskList(List.of(TaskListDataItem.build().withTaskName("task1"), TaskListDataItem.build().withTaskName("task2")));

			projectData.getAspects().add(BeCPGModel.ASPECT_ENTITY_TPL);

			projectData.setEntities(List.of(alfrescoRepository.create(getTestFolderNodeRef(), RawMaterialData.build().withName("MP1")).getNodeRef(),
					alfrescoRepository.create(getTestFolderNodeRef(), RawMaterialData.build().withName("MP2")).getNodeRef()));
			projectData.getEntities().forEach(nodeRef -> entityService.getDocumentsFolder(nodeRef, true));
			alfrescoRepository.create(getTestFolderNodeRef(), projectData);

			// {nodeRef} --> replace with project nodeRef
			// {nodeRef|propName} --> replace with project property
			// {nodeRef|xpath:./path} --> replace with nodeRef found in relative project path
			// {assocName} --> replace with association nodeRef
			// {assocName|propName} --> replace with association property
			// {assocName|@type} --> replace with association property
			// {assocName|xpath:./path} --> replace with nodeRef found in relative assoc path

			projectData.withDeliverableList(List.of(DeliverableListDataItem.build().withTasks(List.of(projectData.getTaskList().get(0).getNodeRef()))

					.withUrl(
							"https://entity?nodeRef={nodeRef}&code={nodeRef|bcpg:code}&type={nodeRef|@type}&documents={nodeRef|xpath:./cm:" + RepoConsts.PATH_DOCUMENTS + "}&state={nodeRef|pjt:projectState?format(.%s)}&entities={pjt:projectEntity}&entityNames={pjt:projectEntity|cm:name}&entityTypes={pjt:projectEntity|@type}&entityDocuments={pjt:projectEntity|xpath:./cm:" + RepoConsts.PATH_DOCUMENTS + "}")));

			projectData = (ProjectData) alfrescoRepository.save(projectData);
			
			entityService.getDocumentsFolder(projectData.getNodeRef(), true);
			
			return projectData;

		});

		inReadTx(() -> {
			
			ProjectData projectData = (ProjectData) alfrescoRepository.findOne(ret.getNodeRef());

			String url = projectScriptHelper.getDeliverableUrl(new ScriptNode(ret.getDeliverableList().get(0).getNodeRef(), serviceRegistry));

			String url2 = "https://entity?nodeRef=" + URLEncoder.encodeUriComponent(ret.getNodeRef().toString())
					+ "&code=" + projectData.getCode() + "&type=" + nodeService.getType(projectData.getNodeRef()).getLocalName()
					+ "&documents=" + URLEncoder.encodeUriComponent(entityService.getDocumentsFolder(projectData.getNodeRef(), false).toString()) + "&state=."
					+ projectData.getState().toString() + "&entities="
					+ URLEncoder.encodeUriComponent(
							projectData.getEntities().stream().map(NodeRef::toString).collect(Collectors.joining(",")))
					+ "&entityNames="
					+ URLEncoder.encodeUriComponent(projectData.getEntities().stream()
							.map(nodeRef -> nodeService.getProperty(nodeRef, ContentModel.PROP_NAME).toString())
							.collect(Collectors.joining(",")))
					+ "&entityTypes=" + URLEncoder.encodeUriComponent(projectData.getEntities().stream()
							.map(nodeService::getType).map(QName::getLocalName).collect(Collectors.joining(",")))
					+ "&entityDocuments="
					+ URLEncoder.encodeUriComponent(projectData.getEntities().stream()
							.map(nodeRef -> entityService.getDocumentsFolder(nodeRef, false).toString())
							.collect(Collectors.joining(",")));

			
			Assert.assertEquals(url, url2);

			return null;
		});

	}

}

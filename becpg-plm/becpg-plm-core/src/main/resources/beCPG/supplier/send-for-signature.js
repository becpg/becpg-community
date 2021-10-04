var projectNode = search.findNode(project.nodeRef);

var entity = projectNode.assocs["pjt:projectEntity"][0];

var supplier = projectNode.assocs["bcpg:suppliers"][0].assocs["bcpg:supplierAccountRef"][0];

var report = bcpg.getReportNode(entity);

var copy = report.getParent().createNode("Signature.pdf", "cm:content");

copy.addAspect("sign:signatureAspect");

copy.createAssociation(supplier, "sign:recipients");

bcpg.copyContent(report, copy);

bcpgArtworks.sendForSignature(copy);

var url = bcpgArtworks.getSignatureViewUrl(copy, supplier);

var deliverables = projectNode.childAssocs["bcpg:entityLists"][0].childByNamePath('deliverableList').children;

var sign = null;

for (var i = 0; i < deliverables.length; i++) {
	
	if (deliverables[i].properties['pjt:dlDescription'] == "Signature") {
		sign = deliverables[i];
		break;
	}

}

sign.properties["pjt:dlUrl"] = url;

sign.save();

var  projectEntity = null;

var projectNode = search.findNode(project.nodeRef);

if (projectNode.assocs["pjt:projectEntity"] != null && projectNode.assocs["pjt:projectEntity"].length > 0)
{
projectEntity = projectNode.assocs["pjt:projectEntity"][0];
} 

bSupplier.assignToSupplier(project, task ,projectEntity, true);

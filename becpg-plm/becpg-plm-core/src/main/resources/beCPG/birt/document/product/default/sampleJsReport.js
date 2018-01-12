//TODO
var docFolders = entity.childByNamePath("Documents");

for each(var node in docFolders.children) {
	pdf.appendPDF(document, node);
}



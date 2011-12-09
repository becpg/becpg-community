<#assign id = args.htmlid>
<script type="text/javascript">//<![CDATA[
   new beCPG.component.DesignerForm("${id}").setOptions(
   {
      modelNodeRef: "${nodeRef!""}"
   }).setMessages(${messages});
//]]></script>
<span id="${id}-dropZone" class="droppable">
  <div id="${id}-dnd-instructions-template" >
      <div id="${id}-dnd-instructions-type" class="instruction">
         <span class="designerInstructionTitle">${msg("dnd.drop.title")}</span>
         <div>
            <div class="designerInstructionColumn designerInstructionColumnRightBorder">
               <img class="designerInstructionImage" src="${url.context}/res/components/model-designer/images/help-drop-list-target-96.png">
               <span class="designerInstructionText">${msg("dnd.drop.moveprop.description")}</span>
            </div>
            <div style="clear:both"></div>
         </div>
      </div>
       <div id="${id}-dnd-instructions-form" class="instruction">
         <span class="designerInstructionTitle">${msg("dnd.drop.title")}</span>
         <div>
            <div class="designerInstructionColumn designerInstructionColumnRightBorder">
               <img class="designerInstructionImage" src="${url.context}/res/components/model-designer/images/help-drop-list-target-96.png">
               <span class="designerInstructionText">${msg("dnd.drop.createfield.description")}</span>
            </div>
            <div class="designerInstructionColumn">
               <img class="designerInstructionImage" src="${url.context}/res/components/model-designer/images/help-drop-folder-target-96.png">
               <span class="designerInstructionText">${msg("dnd.drop.createset.description")}</span>
            </div>
            <div style="clear:both"></div>
         </div>
      </div>
       <div id="${id}-dnd-instructions-field" class="instruction">
         <span class="designerInstructionTitle">${msg("dnd.drop.title")}</span>
         <div>
            <div class="designerInstructionColumn designerInstructionColumnRightBorder">
               <img class="designerInstructionImage" src="${url.context}/res/components/model-designer/images/help-drop-list-target-96.png">
               <span class="designerInstructionText">${msg("dnd.drop.addcontrol.description")}</span>
            </div>
            <div style="clear:both"></div>
         </div>
      </div>
   </div>
</span>
<div class="designer-form" id="${id}-model-form"  >
${msg('model.please-select')}
</div>
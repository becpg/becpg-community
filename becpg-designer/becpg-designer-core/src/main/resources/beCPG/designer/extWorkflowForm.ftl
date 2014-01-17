<config evaluator="string-compare" condition="${engineId}$${processId}">
      <forms>
         <form>
            <field-visibility>
               <show id="bpm:workflowDescription" />
               <show id="bpm:workflowDueDate" />
               <show id="bpm:workflowPriority" />
               <show id="bpm:assignee" />
               <show id="packageItems" />
               <show id="bpm:sendEMailNotifications" />
            </field-visibility>
            <appearance>
               <set id="main" appearance="title" label-id="workflow.set.general" />
               <set id="info" appearance="" template="/org/alfresco/components/form/2-column-set.ftl" />
               <set id="assignee" appearance="title" label-id="workflow.set.assignee" />
               <set id="items" appearance="title" label-id="workflow.set.items" />
               <set id="other" appearance="title" label-id="workflow.set.other" />
               
               <field id="bpm:workflowDescription" label-id="workflow.field.message">
                  <control template="/org/alfresco/components/form/controls/textarea.ftl">
                     <control-param name="style">width: 95%</control-param>
                  </control>
               </field>
               <field id="bpm:workflowDueDate" label-id="workflow.field.due" set="info" />
               <field id="bpm:workflowPriority" label-id="workflow.field.priority" set="info">
                  <control template="/org/alfresco/components/form/controls/workflow/priority.ftl" />
               </field>
               <field id="bpm:assignee" label-id="workflow.field.assign_to" set="assignee" />
               <field id="packageItems" set="items" />
               <field id="bpm:sendEMailNotifications" set="other">
                  <control template="/org/alfresco/components/form/controls/workflow/email-notification.ftl" />
               </field>
            </appearance>
         </form>
      </forms>
   </config>
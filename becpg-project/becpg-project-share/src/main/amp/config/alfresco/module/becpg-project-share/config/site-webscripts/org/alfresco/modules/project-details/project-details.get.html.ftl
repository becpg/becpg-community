


<#assign el=args.htmlid?html>

<@markup id="html">
	<@uniqueIdDiv>
	
<div class="project-details">
        <!-- Begin Evaluation Widget-->
    <div class="white-block">
        <div class="yuiu-b padding-top-5">
            <span class="block-title">${msg("projectdetails.projectevolution")}</span>
            <span class="black-text bold float-right margin-right">${projectDetails.projectName}</span>
        </div>
        <div class= "hr"></div>
        <div id="yui-main">
            <div class="yui-gb display-flex center margin-top">
                <div class="yui-u padding-top-5">
                    <span class="medium-title">${msg("projectdetails.projectevolution.text1")}</span>
                    <span class="big-number red-text">${projectDetails.overdueDays!""} </span>
                    <span class="vertical-top">${msg("projectdetails.projectevolution.dayslate")}</span>
                </div>
                <div class="yui-u">
                    <span class="medium-title margin-left-20">${msg("projectdetails.projectevolution.dueDate")}</span>
                    <span class="vertical-top">${projectDetails.dueDate!""}</span>
                </div>
                <div class="yui-u">
                	<#assign state = projectDetails.state>
                    <span class=" medium-title" <#if state = "InProgress"> style ="color: #17a554
                    	<#elseif state = "Completed"> style ="color: red
                    	<#else> style ="color: blue"</#if>">${msg("projectdetails.project.state.${projectDetails.state}")}</span>
                    <span class="vertical-top">${projectDetails.remainingDays} ${msg("projectdetails.projectevolution.remainingDays")}</span>
                </div>
            </div>
        </div>

        <!-- End of Evaluation Widget-->
        <!-- Begin graph evaluation part-->
	        <div id="yui-main">
	            <div class="chart">
	                <div id="${el}-chart"></div>
	            </div>
	            <div class="yui-b">
	                <div class="yui-u-1-2">
	                    <span class="black-text">${projectDetails.startDate!""}</span>
	                    <span>${msg("projectdetails.start")}</span>
	                <div class="yui float-right">
	                    <span class="black-text float-right">${projectDetails.dueDate!""}</span>
	                    <span class="float-right">${msg("projectdetails.end")} </span>
	                </div>
	            </div>
	        </div>
        </div>
       	<!--End of graph evaluation part-->
    </div>
    <!--Time sheet & miletones & progress statment bars--> 
     <div id="yui-main">
            <div class="white-block">
                <div class="yui-b padding-top-5">
                    <span class="block-title">${msg("projectdetails.progressstatement")}</span>
	                <span class="black-text bold float-right margin-right">${projectDetails.totalTaskNumber} ${msg("projectdetails.progressstatement.tasks")}</span>
            	</div>
                <div class= "hr"></div>
                
                <div class="yui-b">
                    <div id="myProgress">
                        <div id="blueBar" style="width: ${projectDetails.completionPerc}%">
                        	<span class="bar-description"> ${projectDetails.completionPerc}% ${msg("projectdetails.legend.realised")}</span>
                        </div>
                    </div>
                </div>
                <div class="yui-b">
                    <div id="myProgress"><#assign openedWork = 100-projectDetails.completionPerc>
                        <div id="lightBlueBar" style="width: ${openedWork}%">
                        	<span class="bar-description">${openedWork}% ${msg("projectdetails.legend.inprogresswork")}</span>
                        </div>
                    </div>
                </div>
                <div class="yui-b">
                    <div id="myProgress">
                        <div id="redBar" style="width: ${projectDetails.overduePerc?string(",##0")}%">
                        	<span class="bar-description float-right"> ${projectDetails.overduePerc?string(",##0")}% ${msg("projectdetails.legend.overdue")}</span>
                        </div>
                    </div>
                </div>
				<div id = "milestone-box" class="inline-block padding-top-5" style="display: inline-block;">
				    <div class="first-half"> 
		                 <div class="yui-b">
		            		<span class="bold black-text sm-bold-txt margin-top">${projectDetails.milestoneSum} ${msg("projectdetails.progressstatement.milestones")}</span>
		        		 </div>
		        		 <div class="yui-b">
		                	<div id="myProgress">
		    	            	<#assign mlReleasedPerc = projectDetails.milestoneReleased/projectDetails.milestoneSum * 100>
		        	            <div id="blueBar" style="width:${mlReleasedPerc?string(",##0")}%">
		            	        	<span class=" bar-description black-text">${projectDetails.milestoneReleased} ${msg("projectdetails.legend.realised")}</span>
		                	    </div>
		                	</div>
		            	 </div>   
				    </div>
		            <div class="second-half">
			            <div class="yui-u-1 margin-right">
			            	<img src="${url.context}/modules/project-details/images/flag.svg" alt="">
		                    <span class="bold black-text sm-bold-txt margin-top"> ${msg("projectdetails.milestones.nextmilestones")}</span>
		                </div>
		                <div class="yui-u-1">
				                <#if projectDetails.nextMilestoneTask.taskName??>
				                     <span class="black-text bold">${projectDetails.nextMilestoneTask.taskName}</span>
				                     <span class="black-text xs-text margin-top">${projectDetails.nextMilestoneTask.taskStart}</span>
				                <#else><span class="margin-top black-text"> ${msg("projectdetails.no_milestones")}</#if></span>
				        </div>
		      		</div>
			    </div>
		     
     </div>
    
        <div class="white-block">
            <div class="yui-b padding-top-5">
                <span class="block-title">${msg("projectdetails.userprogressstatement")} </span>
                 <span class="black-text bold float-right margin-right">${projectDetails.userTaskSum} ${msg("projectdetails.progressstatement.tasks")}</span>
            </div>
            <div class= "hr"></div>
            <div class="yui-b">
	            <div id="myProgress">
	                <div id="mauveBar" style="width:${projectDetails.userTaskCompletionPerc?string(",##0")}%">
	                    <span class="bar-description">${projectDetails.userTaskCompletionPerc}% ${msg("projectdetails.legend.realised")}</span>
	                </div>
	            </div>
	        </div>
            <div class="yui-b">
            	<#assign restUserTaskCompletionPerc = 100 - projectDetails.userTaskCompletionPerc>
                <div id="myProgress">
                	<div id="lightmauveBar" style="width:${restUserTaskCompletionPerc?string(",##0")}%">
	                    <span class="bar-description">${restUserTaskCompletionPerc}% ${msg("projectdetails.legend.inprogresswork")}</span>
	                </div>
                </div>
            </div>
            <div class="yui-b">
            	<div id="myProgress">
	                <div id="redBar" style="width:${projectDetails.userOverdueTaskCompletionPerc?string(",##0")}%">
	                    <span class="bar-description">${projectDetails.userOverdueTaskCompletionPerc}% ${msg("projectdetails.legend.overdue")}</span>
	                </div>
	            </div>
            </div>
        </div>
     </div>
</div>
 <!-- task listing--> 
<div class="project-taskList-details">
        <div class="yui-b">
            <div class="white-block padding-0">
            <#if projectDetails.taskList.commingTaskList?exists && projectDetails.taskList.commingTaskList?size &gt; 0>
                <div class="green-header yui-skin-sam">
                    <span><img src="${url.context}/modules/project-details/images/check-circle.svg"></span>
                    <span class="title-block adjust-margin">${msg("projectdetails.tasklist.comming")}</span>
                    <span class="white-btn black-text bold filter-btn">${projectDetails.taskList.commingTaskList?size}</span>
                    <span class="float-right">
                		<span id="comming-tasks#comming-tasks" class="onFilterAction">
							<a class="filter-action float-right filter-btn padding-0 btn active">${msg("projectdetails.tasklist.allTasks")}</a>
				    	</span>
				    <span class="float-left">
			        <span id="comming-tasks#my-tasks" class="onFilterAction">
			           <a class="filter-action float-right filter-btn padding-0 btn">${msg("projectdetails.tasklist.myTasks")}</a>
			        </span>
				    </span>
                </div> 
			</div>
		</div>
        <div class="white-block padding-0">
            <div class="yui-b scrollableList"<#if args.height??>style="height: ${args.height}px;"</#if>>
                <#list projectDetails.taskList.commingTaskList as task>
                    <div class="<#if task.isAuthenticatedUser>my-tasks</#if> comming-tasks">
                        <div class="yui-u-1-2 margin-top">
                            <span class="black-text bold float-right margin-right">${task.taskStart}</span>
                        </div>
                        <div class="yui-u-1-2 margin-top">
                            <#if task.isMilestoneTask ><img src="${url.context}/modules/project-details/images/flag.svg" class="margin-left">
							<#else><img src="${url.context}/modules/project-details/images/check.svg" class="margin-left"></#if>
							<span class="black-text"> ${task.taskName}</span>
                        </div>
                    </div>
               </#list>
           </#if>
        </div>
</div>
	<div class="yui-b">
		<div class="white-block padding-0">
		<#if projectDetails.taskList.overdueTaskList?exists && projectDetails.taskList.overdueTaskList?size &gt; 0>
			<div class="red-header yui-skin-sam">
				<span><img src="${url.context}/modules/project-details/images/Exclamation_encircled.svg"></span>
				<span class="title-block adjust-margin">${msg("projectdetails.tasklist.overdue")}</span>
				<span class="white-btn black-text bold filter-btn">${projectDetails.taskList.overdueTaskList?size}</span>
				<span class="float-right">
					<span id="overdue-tasks#overdue-tasks" class="onFilterAction">
			            <a class="filter-action float-right filter-btn active padding-0 btn active">${msg("projectdetails.tasklist.allTasks")}</a>
		        	</span>
		        <span class="float-left">
                	<span id="overdue-tasks#my-tasks" class="onFilterAction">
						<a class="filter-action float-right filter-btn padding-0 btn">${msg("projectdetails.tasklist.myTasks")}</a>
					</span>
		       </span>
			</div>
		</div>
	</div>
		<div class="white-block padding-0">
			<div class="yui-b scrollableList"<#if args.height??>style="height: ${args.height}px;"</#if>>
				<#list projectDetails.taskList.overdueTaskList as task>
					<div class=" <#if task.isAuthenticatedUser>my-tasks</#if> overdue-tasks">
						<div class="yui-u-1-2 margin-top">
							<span class="black-text bold float-right margin-right">${task.taskEnd}</span>
						</div>
						<div class="yui-u-1-2 margin-top">
							<#if task.isMilestoneTask ><img src="${url.context}/modules/project-details/images/flag.svg" class="margin-left">
							<#else><img src="${url.context}/modules/project-details/images/check.svg" class="margin-left"></#if>
							<span class="black-text"> ${task.taskName}</span>
						</div>
					</div>
				</#list>
			</#if>
			</div>
		</div>
	</div>
</div>
<div class="project-comment-details">
    <div class="yui-u-1">
        <div class="white-block div-resizer">
            <div class="yui-b">
                <span><img src="${url.context}/components/images/comment-16.png"/></span>
                <span class="block-title">${msg("projectdetails.comments")} </span>
                <div class= "hr"></div>
                    <#list projectDetails.comments as comment>
                        <div class="comment-details">
                            <div id="main">
                                <div class="yui-gc">
                                    <div class= "yui-u first">
                                        <div class = "main">
                                            <div class = "yui-gf">
                                                <div class= "yui-u first">
                                                    <span><img class = "icon" src ="${url.context}/proxy/alfresco/${comment.commentCreatorAvatar!""}"></span>
                                                </div>
                                                <div class = "yui-u-1">
                                                    <span class="black-text bold comment-creator">${comment.commentCreator}</span>
                                                </div>
                                                <div class = "yui-u-1">
                                                    <span class="black-text comment-content">${comment.commentContent}</span></div>
                                                </div>
                                            </div>
                                    </div>
                                    <div class="yui-u">
                                      <span class="black-text bold float-right margin-right date-info">${comment.commentCreationDate}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </#list>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
      (function(){
     var projectDetails = new beCPG.component.ProjectDetails("${el}").setOptions(
	   {
	   entityNodeRef: "",
	   graphData : [ 	<#list projectDetails.graphData as data>
	   				 ["${data[0]}", ${data[1]}] <#if data_has_next>,</#if>
	   				   </#list>
	   
	    ] 
       
	   });
      })();
</script>


</@>	
</@>
   
/*
 * Copyright (c) 2009, Shlomy Gantz BlueBrick Inc. All rights reserved. Redistribution and use in source and binary
 * forms, with or without modification, are permitted provcurrDivIDed that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provcurrDivIDed with the distribution. * Neither
 * the name of Shlomy Gantz or BlueBrick Inc. nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission. thIS SOFTWARE IS PROvcurrDivIDED BY
 * SHLOMY GANTZ/BLUEBRICK INC. ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, thE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SHLOMY
 * GANTZ/BLUEBRICK INC. BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY thEORY OF LIABILITY, WHEthER IN CONtrACT, StrICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OthERWISE) ARISING IN ANY WAY OUT OF thE USE OF thIS SOFTWARE, EVEN IF ADVISED OF thE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * JSGantt component is a UI control that displays gantt charts based by using CSS and HTML
 * 
 * @module jsgantt
 * @title JSGantt
 */

var JSGantt;
if (!JSGantt) {
   JSGantt = {};
 
}

JSGantt.PREF_GANTT_FORMAT = "fr.becpg.gantt.format";

(function() {

	
	JSGantt.register =  function(parentComponent){
	    JSGantt.scope = parentComponent;
	    
		JSGantt.msg = function (key){
			return Alfresco.util.message.call(parentComponent, key, parentComponent.name, Array.prototype.slice.call(arguments).slice(1));
		};
	};
	
	
	
   /**
    * Creates a task (one row) in gantt object
    * 
    * @class TaskItem
    * @namespace JSGantt
    * @constructor
    * @for JSGantt
    * @param pID
    *            {String} Task unique numeric ID
    * @param pName
    *            {String} Task Name
    * @param pStart
    *            {Date} Task start date/time (not required for pGroup=1 )
    * @param pEnd
    *            {Date} Task end date/time, you can set the end time to 12:00 to indicate half-day (not required for
    *            pGroup=1 )
    * @param pColor
    *            {String} Task bar RGB value
    * @param pLink
    *            {String} Task URL, clicking on the task will redirect to this url. Leave empty if you do not with the
    *            Task also serve as a link
    * @param pMile
    *            {Boolean} Determines whether task is a milestone (1=Yes,0=No)
    * @param pRes
    *            {String} Resource to perform the task
    * @param pComp
    *            {Number} Percent complete (Number between 0 and 100)
    * @param pGroup
    *            {Boolean}
    * @param pParent
    *            {Number} ID of the parent task
    * @param pOpen
    *            {Boolean}
    * @param pDepend
    *            {String} Comma seperated list of IDs this task depends on
    * @param pCaption
    *            {String} Caption to be used instead of default caption (Resource). note : you should use
    *            setCaption("Caption") in order to display the caption
    * @return void
    */	
   JSGantt.TaskItem = function(pID, pName, pStart, pEnd, pColor, pLink, pMile, pRes, pComp, pGroup, pParent, pOpen,
                               pDepend, pCaption, pLineColor) {

	   
	 
	   
      /**
       * the name of the attribute.
       * 
       * @property vcurrDivID
       * @type String
       * @default pID
       * @private
       */
      var vID = pID;

      /**
       * @property vName
       * @type String
       * @default pName
       * @private
       */
      var vName = pName;

      /**
       * @property vStart
       * @type Datetime
       * @default new Date()
       * @private
       */
      var vStart = JSGantt.parseDateStr(pStart, g.getDateInputFormat());

      /**
       * @property vEnd
       * @type Datetime
       * @default new Date()
       * @private
       */
      var vEnd = JSGantt.parseDateStr(pEnd, g.getDateInputFormat());

      /**
       * @property vColor
       * @type String
       * @default pColor
       * @private
       */
      var vColor = pColor;

      /**
       * @property vLineColor
       * @type String
       * @default pColor
       * @private
       */
      var vLineColor = pLineColor;
      
      /**
       * @property vLink
       * @type String
       * @default pLink
       * @private
       */
      var vLink = pLink;

      /**
       * @property vMile
       * @type Boolean
       * @default pMile
       * @private
       */
      var vMile = pMile;

      /**
       * @property vRes
       * @type String
       * @default pRes
       * @private
       */
      var vRes = pRes;

      /**
       * @property vComp
       * @type Number
       * @default pComp
       * @private
       */
      var vComp = pComp;

      /**
       * @property vGroup
       * @type Boolean
       * @default pGroup
       * @private
       */
      var vGroup = pGroup;

      /**
       * @property vParent
       * @type Number
       * @default pParent
       * @private
       */
      var vParent = pParent;

      /**
       * @property vOpen
       * @type Boolean
       * @default pOpen
       * @private
       */
      var vOpen = pOpen;

      /**
       * @property vDepend
       * @type String
       * @default pDepend
       * @private
       */
      var vDepend = pDepend;

      /**
       * @property vCaption
       * @type String
       * @default pCaption
       * @private
       */
      var vCaption = pCaption;

      /**
       * @property vLevel
       * @type Number
       * @default 0
       * @private
       */
      var vLevel = 0;

      /**
       * @property vNumKid
       * @type Number
       * @default 0
       * @private
       */
      var vNumKid = 0;

      /**
       * @property vVisible
       * @type Boolean
       * @default 0
       * @private
       */
      var vVisible = 1;
      


      var x1 = 0, y1 = 0, x2 = 0, y2 = 0;

      
   
      /**
       * Returns task ID
       * 
       * @method getID
       * @return {String}
       */
      this.getID = function() {
         return vID;
      };
      /**
       * Returns task name
       * 
       * @method getName
       * @return {String}
       */
      this.getName = function() {
         return vName;
      };
      /**
       * Returns task start date
       * 
       * @method getStart
       * @return {Datetime}
       */
      this.getStart = function() {
         return vStart;
      };
      /**
       * Returns task end date
       * 
       * @method getEnd
       * @return {Datetime}
       */
      this.getEnd = function() {
         return vMile ? vStart : vEnd;
      };

      /**
       * Returns task bar color (i.e. 00FF00)
       * 
       * @method getColor
       * @return {String}
       */
      this.getColor = function() {
         return vColor;
      };

      /**
       * Returns Line color (i.e. 00FF00)
       * 
       * @method getColor
       * @return {String}
       */
      this.getLineColor = function() {
         return vLineColor;
      };
      
      /**
       * Returns task URL (i.e. http://www.jsgantt.com)
       * 
       * @method getLink
       * @return {String}
       */
      this.getLink = function() {
         return vLink;
      };

      /**
       * Returns whether task is a milestone (1=Yes,0=No)
       * 
       * @method getMile
       * @return {Boolean}
       */
      this.getMile = function() {
         return vMile;
      };

      /**
       * Returns task dependencies as list of values (i.e. 123,122)
       * 
       * @method getDepend
       * @return {String}
       */
      this.getDepend = function() {
         if (vDepend) {
            return vDepend;
         }
         return null;
      };

      /**
       * Returns task caption (if it exists)
       * 
       * @method getCaption
       * @return {String}
       */
      this.getCaption = function() {
         if (vCaption) {
            return vCaption;
         }
         return '';
      };

      /**
       * Returns task resource name as string
       * 
       * @method getResource
       * @return {String}
       */
      this.getResource = function() {
         if(typeof (vRes) !== 'undefined' && vRes !== null) {
            return vRes;
         }
         return '&nbsp;';

      };

      /**
       * Returns task completion percent as numeric value
       * 
       * @method getCompVal
       * @return {Integer}
       */
      this.getCompVal = function() {
         if (vComp) {
            return vComp;
         }
         return 0;

      };

      /**
       * Returns task completion percent as formatted string (##%)
       * 
       * @method getCompStr
       * @return {String}
       */
      this.getCompStr = function() {
         if (vComp) {
            return vComp + '%';
         }
         return '';
      };

      /**
       * Returns task duration as a fortmatted string based on the current selected format
       * 
       * @method getDuration
       * @param vFormat
       *            {String} selected format (minute,hour,day,week,month)
       * @return {String}
       */
      this.getDuration = function(vFormat) {
         var tmpPer = null, vDuration = null;

         if (vMile) {
            vDuration = '-';
         } else if (vFormat === 'hour') {
            tmpPer = Math.ceil((this.getEnd() - this.getStart()) / (60 * 60 * 1000));
            if (tmpPer === 1) {
               vDuration = '1 '+JSGantt.msg("jsgantt.hour");
            } else {
               vDuration = tmpPer + ' '+JSGantt.msg("jsgantt.hours");
            }
         }

         else if (vFormat === 'minute') {
            tmpPer = Math.ceil((this.getEnd() - this.getStart()) / (60 * 1000));
            if (tmpPer === 1) {
               vDuration = '1 '+JSGantt.msg("jsgantt.minute");
            } else {
               vDuration = tmpPer + ' '+JSGantt.msg("jsgantt.minutes");
            }
         }

         else { // if(vFormat == 'day') {
            tmpPer = Math.ceil((this.getEnd() - this.getStart()) / (24 * 60 * 60 * 1000) + 1);
            if (tmpPer === 1) {
               vDuration = '1 '+JSGantt.msg("jsgantt.day");
            } else {
               vDuration = tmpPer + ' '+JSGantt.msg("jsgantt.days");
            }
         }

         return vDuration;
      };

      /**
       * Returns task parent ID
       * 
       * @method getParent
       * @return {Number}
       */
      this.getParent = function() {
         return vParent;
      };

      /**
       * Returns whether task is a group (1=Yes,0=No)
       * 
       * @method getGroup
       * @return {Integer}
       */
      this.getGroup = function() {
         return vGroup;
      };

      /**
       * Returns whether task is open (1=Yes,0=No)
       * 
       * @method getOpen
       * @return {Integer}
       */
      this.getOpen = function() {
         return vOpen;
      };

      /**
       * Returns task tree level (0,1,2,3...)
       * 
       * @method getLevel
       * @return {Integer}
       */
      this.getLevel = function() {
         return vLevel;
      };

      /**
       * Returns the number of child tasks
       * 
       * @method getNumKids
       * @return {Number}
       */
      this.getNumKids = function() {
         return vNumKid;
      };
      /**
       * Returns the X position of the left side of the task bar on the graph (right side)
       * 
       * @method getStartX
       * @return {Number}
       */
      this.getStartX = function() {
         return x1;
      };

      /**
       * Returns the Y position of the top of the task bar on the graph (right side)
       * 
       * @method getStartY
       * @return {Number}
       */
      this.getStartY = function() {
         return y1;
      };

      /**
       * Returns the X position of the right of the task bar on the graph (right side)
       * 
       * @method getEndX
       * @return {Int}
       */
      this.getEndX = function() {
         return x2;
      };

      /**
       * Returns the Y position of the bottom of the task bar on the graph (right side)
       * 
       * @method getEndY
       * @return {Number}
       */
      this.getEndY = function() {
         return y2;
      };

      /**
       * Returns whether task is visible (1=Yes,0=No)
       * 
       * @method getVisible
       * @return {Boolean}
       */
      this.getVisible = function() {
         return vVisible;
      };

      /**
       * Set task dependencies
       * 
       * @method setDepend
       * @param pDepend
       *            {String} A comma delimited list of task IDs the current task depends on.
       * @return {void}
       */
      this.setDepend = function(ppDepend) {
         vDepend = ppDepend;
      };

      /**
       * Set task start date/time
       * 
       * @method setStart
       * @param pStart
       *            {Datetime}
       * @return {void}
       */
      this.setStart = function(ppStart) {
         vStart = ppStart;
      };

      /**
       * Set task end date/time
       * 
       * @method setEnd
       * @param pEnd
       *            {Datetime}
       * @return {void}
       */
      this.setEnd = function(ppEnd) {
         vEnd = ppEnd;
      };

      /**
       * Set task tree level (0,1,2,3...)
       * 
       * @method setLevel
       * @param pLevel
       *            {Number}
       * @return {void}
       */
      this.setLevel = function(pLevel) {
         vLevel = pLevel;
      };

      /**
       * Set Number of children for the task
       * 
       * @method setNumKid
       * @param pNumKid
       *            {Number}
       * @return {void}
       */
      this.setNumKid = function(pNumKid) {
         vNumKid = pNumKid;
      };

      /**
       * Set task completion percentage
       * 
       * @method setCompVal
       * @param pCompVal
       *            {Number}
       * @return {void}
       */
      this.setCompVal = function(pCompVal) {
         vComp = pCompVal;
      };

      /**
       * Set a task bar starting position (left)
       * 
       * @method setStartX
       * @param pX
       *            {Number}
       * @return {void}
       */
      this.setStartX = function(pX) {
         x1 = pX;
      };

      /**
       * Set a task bar starting position (top)
       * 
       * @method setStartY
       * @param pY
       *            {Number}
       * @return {String}
       */
      this.setStartY = function(pY) {
         y1 = pY;
      };

      /**
       * Set a task bar starting position (right)
       * 
       * @method setEndX
       * @param pX
       *            {Number}
       * @return {String}
       */
      this.setEndX = function(pX) {
         x2 = pX;
      };

      /**
       * Set a task bar starting position (bottom)
       * 
       * @method setEndY
       * @param pY
       *            {Number}
       * @return {String}
       */
      this.setEndY = function(pY) {
         y2 = pY;
      };

      /**
       * Set task open/closed
       * 
       * @method setOpen
       * @param pOpen
       *            {Boolean}
       * @return {void}
       */
      this.setOpen = function(ppOpen) {
         vOpen = ppOpen;
      };

      /**
       * Set task visibility
       * 
       * @method setVisible
       * @param pVisible
       *            {Boolean}
       * @return {void}
       */
      this.setVisible = function(pVisible) {
         vVisible = pVisible;
      };

   };

   /**
    * Creates the gant chart. for example:
    * <p>
    * var g = new JSGantt.GanttChart('g',document.getElementById('GanttChartdiv'), 'day');
    * </p>
    * var g = new JSGantt.GanttChart( - assign the gantt chart to a javascript variable called 'g' 'g' - the name of the
    * variable that was just assigned (will be used later so that gantt object can reference itself)
    * document.getElementById('GanttChartdiv') - reference to the div that will hold the gantt chart 'day' - default
    * format will be by day
    * 
    * @class GanttChart
    * @param pGanttVar
    *            {String} the name of the gantt chart variable
    * @param pDiv
    *            {String} reference to the div that will hold the gantt chart
    * @param pFormat
    *            {String} default format (minute,hour,day,week,month,quarter)
    * @return void
    */

   JSGantt.GanttChart = function(pGanttVar, pDiv, pFormat) {
       
       
      var preferences = new Alfresco.service.Preferences();
       
      /**
       * the name of the gantt chart variable
       * 
       * @property vGanttVar
       * @type String
       * @default pGanttVar
       * @private
       */
      var vGanttVar = pGanttVar;
      /**
       * the name of the gantt chart div
       * 
       * @property vDiv
       * @type String
       * @default pDiv
       * @private
       */
      var vDiv = pDiv;
      /**
       * Selected format (minute,hour,day,week,month)
       * 
       * @property vFormat
       * @type String
       * @default pFormat
       * @private
       */
      var vFormat = 'day';
      
      if(pFormat != null && pFormat.length>0){
          vFormat = pFormat;
      } else {
          vFormat = Alfresco.util.findValueByDotNotation(preferences.get(), JSGantt.PREF_GANTT_FORMAT);
          if(!vFormat){
              vFormat = 'day';
          }
      }
      
      
      /**
       * Show resource column
       * 
       * @property vShowRes
       * @type Number
       * @default 1
       * @private
       */
      var vShowRes = 0;
      /**
       * Show duration column
       * 
       * @property vShowDur
       * @type Number
       * @default 1
       * @private
       */
      var vShowDur = 1;
      /**
       * Show percent complete column
       * 
       * @property vShowComp
       * @type Number
       * @default 1
       * @private
       */
      var vShowComp = 0;
      /**
       * Show start date column
       * 
       * @property vShowStartDate
       * @type Number
       * @default 1
       * @private
       */
      var vShowStartDate = 0;
      /**
       * Show end date column
       * 
       * @property vShowEndDate
       * @type Number
       * @default 1
       * @private
       */
      var vShowEndDate = 0;
      /**
       * Show select checkbox
       * 
       * @property vShowSelect
       * @type Number
       * @default 0
       * @private
       */
      var  vShowSelect = 1;
      
      /**
       * Factor to augment col width
       * 
       * @property vColWidthFactor
       * @type Number
       * @default 1
       * @private
       */
      var vColWidthFactor = 1;
      /**
       * Date input format
       * 
       * @property vDateInputFormat
       * @type String
       * @default "mm/dd/yyyy"
       * @private
       */
      var vDateInputFormat = "mm/dd/yyyy";
      /**
       * Date display format
       * 
       * @property vDateDisplayFormat
       * @type String
       * @default "mm/dd/yy"
       * @private
       */
      var vDateDisplayFormat = "mm/dd/yy";

      var vNumUnits = 0;
      var vCaptionType = "";
      var vDepId = 1;
      var vTaskList = [];
      var vFormatArr = [ "day", "week", "month", "quarter" ];
      var vQuarterArr = [ 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4 ];
      var vMonthDaysArr = [ 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 ];
	  //var vMonthWeeksArr = [5,  4,  4,  5,  4,  4,  5,  4,  5,  4,  4,  4];    
      var vMonthArr = Alfresco.util.message("months.long").split(",");
      

      /**
       * Set current display format (minute/hour/day/week/month/quarter) Only the first 4 arguments are used, for
       * example: <code>
       * g.setFormatArr("day","week","month");
       * </code> will show 3 formatting options (day/week/month)
       * at the bottom right of the gantt chart
       * 
       * @method setFormatArr
       * @return {void}
       */
      this.setFormatArr = function() {
    	  vFormatArr = new Array();
         for ( var i = 0; i < arguments.length; i++) {
            vFormatArr[i] = arguments[i];
         }
         if (vFormatArr.length > 4) {
            vFormatArr.length = 4;
         }
      };
      /**
       * Show/Hide resource column
       * 
       * @param pShow
       *            {Number} 1=Show,0=Hide
       * @method setShowRes
       * @return {void}
       */
      this.setShowRes = function(pShow) {
         vShowRes = pShow;
      };
      /**
       * Show/Hide duration column
       * 
       * @param pShow
       *            {Number} 1=Show,0=Hide
       * @method setShowDur
       * @return {void}
       */
      this.setShowDur = function(pShow) {
         vShowDur = pShow;
      };
      /**
       * Show/Hide duration column
       * 
       * @param pShow
       *            {Number} 1=Show,0=Hide
       * @method setShowDur
       * @return {void}
       */
      this.setShowSelect = function(pShowSelect) {
         vShowSelect = pShowSelect;
      };
      /**
       * Show/Hide duration column
       * 
       * @param pShow
       *            {Number} 1=Show,0=Hide
       * @method setShowDur
       * @return {void}
       */
      this.setColWidthFactor = function(pColWidthFactor) {
          vColWidthFactor = pColWidthFactor;
      };
      /**
       * Show/Hide completed column
       * 
       * @param pShow
       *            {Number} 1=Show,0=Hide
       * @method setShowComp
       * @return {void}
       */
      this.setShowComp = function(pShow) {
         vShowComp = pShow;
      };
      /**
       * Show/Hide start date column
       * 
       * @param pShow
       *            {Number} 1=Show,0=Hide
       * @method setShowStartDate
       * @return {void}
       */
      this.setShowStartDate = function(pShow) {
         vShowStartDate = pShow;
      };
      /**
       * Show/Hide end date column
       * 
       * @param pShow
       *            {Number} 1=Show,0=Hide
       * @method setShowEndDate
       * @return {void}
       */
      this.setShowEndDate = function(pShow) {
         vShowEndDate = pShow;
      };
      /**
       * Overall date input format
       * 
       * @param pShow
       *            {String} (mm/dd/yyyy,dd/mm/yyyy,yyyy-mm-dd)
       * @method setDateInputFormat
       * @return {void}
       */
      this.setDateInputFormat = function(pShow) {
         vDateInputFormat = pShow;
      };
      /**
       * Overall date display format
       * 
       * @param pShow
       *            {String} (mm/dd/yyyy,dd/mm/yyyy,yyyy-mm-dd)
       * @method setDateDisplayFormat
       * @return {void}
       */
      this.setDateDisplayFormat = function(pShow) {
         vDateDisplayFormat = pShow;
      };
      /**
       * Set gantt caption
       * 
       * @param pType
       *            {String}
       *            <p>
       *            Caption-Displays a custom caption set in TaskItem<br>
       *            Resource-Displays task resource<br>
       *            Duration-Displays task duration<br>
       *            Complete-Displays task percent complete
       *            </p>
       * @method setCaptionType
       * @return {void}
       */
      this.setCaptionType = function(pType) {
         vCaptionType = pType;
      };
      /**
       * Set current display format and redraw gantt chart (minute/hour/day/week/month/quarter)
       * 
       * @param pFormat
       *            {String} (mm/dd/yyyy,dd/mm/yyyy,yyyy-mm-dd)
       * @method setFormat
       * @return {void}
       */
      this.setFormat = function(ppFormat) {
         vFormat = ppFormat;
         preferences.set(JSGantt.PREF_GANTT_FORMAT,ppFormat);
         this.Draw();
      };
      
      this.getFormat = function() {     
         return  vFormat;
       };
      /**
       * Returns whether resource column is shown
       * 
       * @method getShowRes
       * @return {Number}
       */
      this.getShowRes = function() {
         return vShowRes;
      };
      /**
       * Returns whether duration column is shown
       * 
       * @method getShowDur
       * @return {Number}
       */
      this.getShowDur = function() {
         return vShowDur;
      };
      /**
       * Returns whether percent complete column is shown
       * 
       * @method getShowComp
       * @return {Number}
       */
      this.getShowComp = function() {
         return vShowComp;
      };
      /**
       * Returns whether start date column is shown
       * 
       * @method getShowStartDate
       * @return {Number}
       */
      this.getShowStartDate = function() {
         return vShowStartDate;
      };
      /**
       * Returns whether end date column is shown
       * 
       * @method getShowEndDate
       * @return {Number}
       */
      this.getShowEndDate = function() {
         return vShowEndDate;
      };
      /**
       * Returns date input format
       * 
       * @method getDateInputFormat
       * @return {String}
       */
      this.getDateInputFormat = function() {
         return vDateInputFormat;
      };
      /**
       * Returns current display format
       * 
       * @method getDateDisplayFormat
       * @return {String}
       */
      this.getDateDisplayFormat = function() {
         return vDateDisplayFormat;
      };
      /**
       * Returns current gantt caption type
       * 
       * @method getCaptionType
       * @return {String}
       */
      this.getCaptionType = function() {
         return vCaptionType;
      };
      /**
       * Calculates X/Y coordinates of a task and sets the Start and End properties of the TaskItem
       * 
       * @method CalcTaskXY
       * @return {Void}
       */
      this.CalcTaskXY = function() {
         var vList = this.getList();

         for ( var i = 0; i < vList.length; i++) {
            vcurrDivID = vList[i].getID();
            vTaskDiv = document.getElementById("taskbar_" + vcurrDivID);
            vBarDiv = document.getElementById("bardiv_" + vcurrDivID);
            vParDiv = document.getElementById("childgrid_" + vcurrDivID);

            if (vBarDiv) {
               vList[i].setStartX(vBarDiv.offsetLeft);
               vList[i].setStartY(vParDiv.offsetTop + vBarDiv.offsetTop + 6);
               vList[i].setEndX(vBarDiv.offsetLeft + vBarDiv.offsetWidth);
               vList[i].setEndY(vParDiv.offsetTop + vBarDiv.offsetTop + 6);
            }

         }
      };

      /**
       * Adds a TaskItem to the Gantt object task list array
       * 
       * @method AddTaskItem
       * @return {Void}
       */
      this.AddTaskItem = function(value) {
         vTaskList.push(value);
      };
      /**
       * Returns task list Array
       * 
       * @method getList
       * @return {Array}
       */
      this.getList = function() {
         return vTaskList;
      };

      /**
       * Clears dependency lines between tasks
       * 
       * @method clearDependencies
       * @return {Void}
       */
      this.clearDependencies = function() {
         var divParent = document.getElementById('rightside');
         var depLine;
         var vMaxId = vDepId;
         for ( var i = 1; i < vMaxId; i++) {
            depLine = document.getElementById("line" + i);
            if (depLine) {
               divParent.removeChild(depLine);
            }
         }
         vDepId = 1;
      };

      this.scrollToY = function(yPos, vDiv) {
	         var size = parseInt(YAHOO.util.Dom.getStyle(vDiv, 'width').replace("px",""), 10);
	         var col1 = YAHOO.util.Dom.get('leftside');
	         var scrollDiv = YAHOO.util.Dom.get('rightside');
	         var max = (size - 350);
	         var resize = new YAHOO.util.Resize('leftside', {
	             handles: ['r'],
	             minWidth: 350,
	             maxWidth: max
	         });
	         resize.on('resize', function(ev) {
	             var w = ev.width;
	             var sizeDiv = parseInt(YAHOO.util.Dom.getStyle(vDiv, 'width').replace("px",""), 10);
	             YAHOO.util.Dom.setStyle(col1, 'height', '');
	             YAHOO.util.Dom.setStyle(scrollDiv, 'width', (sizeDiv - w - 6) + 'px');
	             
	             var nodes = YAHOO.util.Selector.query('td.ggTaskTitle span.task-title a.theme-color-1');
	             if(nodes.length>0){
	            	 var tdNode = nodes[0].parentNode.parentNode.parentNode.parentNode;
	            	 var sizeTd = YAHOO.util.Dom.getRegion(tdNode);
		             for(var i in nodes){  	 
		            	 YAHOO.util.Dom.setStyle(nodes[i], 'max-width', (sizeTd.width - 110) + 'px');
		             }
	             }
	             
	         });
	
	         
         if(yPos){
            scrollDiv.scrollLeft = yPos;
         }
      };

      /**
       * Draw a straight line (colored one-pixel wide div), need to parameterize doc item
       * 
       * @method sLine
       * @return {Void}
       */
      this.sLine = function(x1, y1, x2, y2) {

         vLeft = Math.min(x1, x2);
         vTop = Math.min(y1, y2);
         vWid = Math.abs(x2 - x1) + 1;
         vHgt = Math.abs(y2 - y1) + 1;

         vDoc = document.getElementById('rightside');

         // retrieve div
         var oDiv = document.createElement('div');
         oDiv.className = "ggline";
         oDiv.id = "line" + vDepId++;
                
         oDiv.style.left = vLeft + "px";
         oDiv.style.top = vTop + "px";
         oDiv.style.width = vWid + "px";
         oDiv.style.height = vHgt + "px";

         vDoc.appendChild(oDiv);

      };

      /**
       * Draw a diaganol line (calc line x,y pairs and draw multiple one-by-one sLines)
       * 
       * @method dLine
       * @return {Void}
       */
      this.dLine = function(x1, y1, x2, y2) {

         var dx = x2 - x1;
         var dy = y2 - y1;
         var x = x1;
         var y = y1;

         var n = Math.max(Math.abs(dx), Math.abs(dy));
         dx = dx / n;
         dy = dy / n;
         for ( var i = 0; i <= n; i++) {
            vx = Math.round(x);
            vy = Math.round(y);
            this.sLine(vx, vy, vx, vy);
            x += dx;
            y += dy;
         }

      };

      /**
       * Draw dependency line between two points (task 1 end -> task 2 start)
       * 
       * @method drawDependency
       * @return {Void}
       */
      this.drawDependency = function(x1, y1, x2, y2) {
         if (x1 + 10 < x2) {
            this.sLine(x1, y1, x1 + 4, y1);
            this.sLine(x1 + 4, y1, x1 + 4, y2);
            this.sLine(x1 + 4, y2, x2, y2);
            this.dLine(x2, y2, x2 - 3, y2 - 3);
            this.dLine(x2, y2, x2 - 3, y2 + 3);
            this.dLine(x2 - 1, y2, x2 - 3, y2 - 2);
            this.dLine(x2 - 1, y2, x2 - 3, y2 + 2);
         } else {
            this.sLine(x1, y1, x1 + 4, y1);
            this.sLine(x1 + 4, y1, x1 + 4, y2 - 10);
            this.sLine(x1 + 4, y2 - 10, x2 - 8, y2 - 10);
            this.sLine(x2 - 8, y2 - 10, x2 - 8, y2);
            this.sLine(x2 - 8, y2, x2, y2);
            this.dLine(x2, y2, x2 - 3, y2 - 3);
            this.dLine(x2, y2, x2 - 3, y2 + 3);
            this.dLine(x2 - 1, y2, x2 - 3, y2 - 2);
            this.dLine(x2 - 1, y2, x2 - 3, y2 + 2);
         }
      };

      /**
       * Draw all task dependencies
       * 
       * @method DrawDependencies
       * @return {Void}
       */
      this.DrawDependencies = function() {

         // First recalculate the x,y
         this.CalcTaskXY();

         this.clearDependencies();

         var vList = this.getList();
         for ( var i = 0; i < vList.length; i++) {

            vDepend = vList[i].getDepend();
            if (vDepend) {

               var vDependStr = vDepend + '';
               var vDepList = vDependStr.split(',');
               var n = vDepList.length;

               for ( var k = 0; k < n; k++) {
                  var vTask = this.getArrayLocationByID(vDepList[k]);

                  if (vTask!=null && vList[vTask].getVisible() == 1 && vList[i].getVisible() == 1){
                      
                      if(vList[vTask].getMile()) {
                          this.drawDependency(vList[vTask].getEndX() - 251, vList[vTask].getEndY()+1, vList[i].getStartX()-1,
                                  vList[i].getStartY()+1);
                      } else {
                          this.drawDependency(vList[vTask].getEndX()+1, vList[vTask].getEndY()+1, vList[i].getStartX() - 1,
                                  vList[i].getStartY()+1);
                      }
                      
                   
                  }
               }
            }
         }
      };

      /**
       * Find location of TaskItem based on the task ID
       * 
       * @method getArrayLocationByID
       * @return {Void}
       */
      this.getArrayLocationByID = function(pId) {

         var vList = this.getList();
         for ( var i = 0; i < vList.length; i++) {
            if (vList[i].getID() == pId)
               return i;
         }
         
         return null;
      };

      /**
       * Draw gantt chart
       * 
       * @method Draw
       * @return {Void}
       */
      this.Draw = function() {
          
         try { 
         var vMaxDate = new Date();
         var vMinDate = new Date();
         var vTmpDate = new Date();
         var vNxtDate = new Date();
         var vCurrDate = new Date();
         var vCurrPosY = 0;
         var vTaskLeft = 0;
         var vTaskRight = 0;
         var vcurrDivID = 0;
         var vMainTable = "";
         var vLeftTable = "";
         var vRightTable = "";
         var vDateRowStr = "";
         var vItemRowStr = "";
         var vColWidth = 0;
         var vColUnit = 0;
         var vChartWidth = 0;
         var vNumDays = 0;
         var vDayWidth = 0;
         var vStr = "";
         var colNumber = 2;
         
          if (vShowRes === 1){
        	 colNumber++;
          }
          if (vShowDur === 1) {
        	  colNumber++;
          }
          if (vShowComp === 1) {
        	  colNumber++;
       	  }
          if (vShowStartDate === 1) {
        	  colNumber++;
          }
          if (vShowEndDate === 1) {
        	  colNumber++;
          }

         if (vTaskList.length > 0) {

            // Process all tasks preset parent date and completion %
            JSGantt.processRows(vTaskList, 0, -1, 1, 1);

            // get overall min/max dates plus padding
            vMinDate = JSGantt.getMinDate(vTaskList, vFormat);
            vMaxDate = JSGantt.getMaxDate(vTaskList, vFormat);

            // Calculate chart width variables. vColWidth can be altered
            // manually
            // to change each column width
            // May be smart to make this a parameter of GanttChart or set it
            // based
            // on existing pWidth parameter
            if (vFormat == 'day') {
               vColWidth = 22 * vColWidthFactor;
               vColUnit = 1;
            } else if (vFormat == 'week') {
               vColWidth = 37 * vColWidthFactor;
               vColUnit = 7;
            } else if (vFormat == 'month') {
               vColWidth = 37 * vColWidthFactor;
               vColUnit = 30;
            } else if (vFormat == 'quarter') {
               vColWidth = 60 * vColWidthFactor;
               vColUnit = 90;
            }else if (vFormat == 'hour') {
               vColWidth = 22 * vColWidthFactor;
               vColUnit = 1;
            } else if (vFormat == 'minute') {
               vColWidth = 22 * vColWidthFactor;
               vColUnit = 1;
            }

            vNumDays = (vMaxDate.getTime() - vMinDate.getTime()) / (24 * 60 * 60 * 1000);
            vNumUnits = vNumDays / vColUnit;

            vChartWidth = vNumUnits * vColWidth + 1;
            vDayWidth = (vColWidth / vColUnit) + (1 / vColUnit);

            vMainTable = '<div class="yui-gd yui-dt">';

            // DRAW the Left-side of the chart (names, resources, comp%)
            vLeftTable = '<div class="yui-u first" id="leftside"><table cellSpacing="0" cellPadding="0" border="0"><colgroup>';
            	
            vLeftTable +='<col class="ggtaskCheckbox"/><col class="ggTaskTitle  ggCol'+colNumber+'"></col>';
            
            if (vShowRes === 1){
            	 vLeftTable +='<col class="ggCol'+colNumber+'"></col>';
             }
             if (vShowDur === 1) {
            	 vLeftTable +='<col class="ggCol'+colNumber+'"></col>';
             }
             if (vShowComp === 1) {
            	 vLeftTable +='<col class="ggCol'+colNumber+'"></col>';
          	  }
             if (vShowStartDate === 1) {
            	 vLeftTable +='<col class="ggCol'+colNumber+'"></col>';
             }
             if (vShowEndDate === 1) {
            	 vLeftTable +='<col class="ggCol'+colNumber+'"></col>';
             }
            
            	
            vLeftTable += '</colgroup><thead><tr class="gheader">';
            
//            if (vShowSelect === 1 ) {
//                vLeftTable +="<th width=\"16px\">";
//                
//                vLeftTable+="<div class=\"datagrid\" style=\"visibility:visible;\"><div id=\""+JSGantt.scope.id+"-JSGantt-itemSelect-div\" class=\"item-select hidden\">";
//                vLeftTable+="<button id=\""+JSGantt.scope.id+"-JSGantt-itemSelect-button\" name=\"JSGantt-itemSelect-button\">&nbsp;</button>";
//                vLeftTable+="<div id=\""+JSGantt.scope.id+"-JSGantt-itemSelect-menu\" class=\"yuimenu\">";
//                vLeftTable+="   <div class=\"bd\">";
//                vLeftTable+="      <ul>";
//                vLeftTable+="         <li><a href=\"#\"><span class=\"selectAll\">"+JSGantt.msg("menu.select.all")+"</span></a></li>";
//                vLeftTable+="         <li><a href=\"#\"><span class=\"selectInvert\">"+JSGantt.msg("menu.select.invert")+"</span></a></li>";
//                vLeftTable+="         <li><a href=\"#\"><span class=\"selectNone\">"+JSGantt.msg("menu.select.none")+"</span></a></li>";
//                vLeftTable+="      </ul>";
//                vLeftTable+="   </div>";
//                vLeftTable+=" </div>";
//                vLeftTable+=" </div></div>";
//                
//                vLeftTable +="</th>";
//            }
            
            vLeftTable += '<th  colspan="'+colNumber+'">'+JSGantt.msg("jsgantt.format")+':&nbsp;';
       

            if (vFormatArr.join().indexOf("minute") != -1) {
               if (vFormat == 'minute')
                  vLeftTable += '<input type="radio" name="radFormat" value="minute" checked>'+JSGantt.msg("jsgantt.minute");
               else
                  vLeftTable += '<input type="radio" name="radFormat" onclick="JSGantt.changeFormat(\'minute\',' + vGanttVar + ');" value="minute">'+JSGantt.msg("jsgantt.minute");
            }

            if (vFormatArr.join().indexOf("hour") != -1) {
               if (vFormat == 'hour')
                  vLeftTable += '<input type="radio" name="radFormat" value="hour" checked>'+JSGantt.msg("jsgantt.hour");
               else
                  vLeftTable += '<input type="radio" name="radFormat" onclick="JSGantt.changeFormat(\'hour\',' + vGanttVar + ');" value="hour">'+JSGantt.msg("jsgantt.hour");
            }

            if (vFormatArr.join().indexOf("day") != -1) {
               if (vFormat == 'day')
                  vLeftTable += '<input type="radio" name="radFormat" value="day" checked>'+JSGantt.msg("jsgantt.day");
               else
                  vLeftTable += '<input type="radio" name="radFormat" onclick="JSGantt.changeFormat(\'day\',' + vGanttVar + ');" value="day">'+JSGantt.msg("jsgantt.day");
            }

            if (vFormatArr.join().indexOf("week") != -1) {
               if (vFormat == 'week')
                  vLeftTable += '<input type="radio" name="radFormat" value="week" checked>'+JSGantt.msg("jsgantt.week");
               else
                  vLeftTable += '<input type="radio" name="radFormat" onclick="JSGantt.changeFormat(\'week\',' + vGanttVar + ');" value="week">'+JSGantt.msg("jsgantt.week");
            }

            if (vFormatArr.join().indexOf("month") != -1) {
               if (vFormat == 'month')
                  vLeftTable += '<input type="radio" name="radFormat" value="month" checked>'+JSGantt.msg("jsgantt.month");
               else
                  vLeftTable += '<input type="radio" name="radFormat" onclick="JSGantt.changeFormat(\'month\',' + vGanttVar + ');" value="month">'+JSGantt.msg("jsgantt.month");
            }

            if (vFormatArr.join().indexOf("quarter") != -1) {
               if (vFormat == 'quarter')
                  vLeftTable += '<input type="radio" name="radFormat" value="quarter" checked>'+JSGantt.msg("jsgantt.quarter");
               else
                  vLeftTable += '<input type="radio" name="radFormat" onclick="JSGantt.changeFormat(\'quarter\',' + vGanttVar + ');" value="quarter">'+JSGantt.msg("jsgantt.quarter");
            }

            // vLeftTable += '<input type="radio" name="other" value="other"
            // style="display:none"> .';

            vLeftTable += '</th></tr>';

            vLeftTable += '<tr class="gheader">' + ' <th colspan="2"><nobr></nobr></th>';

           
            
            if (vShowRes === 1){
               vLeftTable += '  <th class="nowrap" >'+JSGantt.msg("jsgantt.resource")+'</th>';
            }
            if (vShowDur === 1) {
               vLeftTable += '  <th  class="nowrap" >'+JSGantt.msg("jsgantt.duration")+'</th>';
            }
            if (vShowComp === 1) {
               vLeftTable += '  <th  class="nowrap" >'+JSGantt.msg("jsgantt.percomp")+'</th>';
         	}
            if (vShowStartDate === 1) {
               vLeftTable += '  <th class="nowrap" >'+JSGantt.msg("jsgantt.begin")+'</th>';
            }
            if (vShowEndDate === 1) {
               vLeftTable += '  <th>'+JSGantt.msg("jsgantt.end")+'</th>';
            }

            vLeftTable += '</tr></thead><tbody class="yui-dt-data">';

            for ( var i = 0; i < vTaskList.length; i++) {
                
               if (vTaskList[i].getGroup()) {
                  vRowType = "group";
               } else {
                  vRowType = "row";
               }

               vcurrDivID = vTaskList[i].getID();
               
               var vLineColorStyle = vTaskList[i].getLineColor()? 'style="background-color:' 
                  + vTaskList[i].getLineColor() + '"' :"";
               
               if (vTaskList[i].getVisible() === 0) {
                  vLeftTable += '<tr id="child_' + vcurrDivID + '" class="g' + vRowType + ' hidden" '+vLineColorStyle+' onMouseover="g.mouseOver(this,\'' + vcurrDivID + '\',\'left\',\'' + vRowType + '\')" onMouseout="g.mouseOut(this,\'' + vcurrDivID + '\',\'left\',\'' + vRowType + '\')">';
               } else {
                  vLeftTable += '<tr id="child_' + vcurrDivID + '"  class="g' + vRowType + '" '+vLineColorStyle+' onMouseover="g.mouseOver(this,\'' + vcurrDivID + '\',\'left\',\'' + vRowType + '\')" onMouseout="g.mouseOut(this,\'' + vcurrDivID + '\',\'left\',\'' + vRowType + '\')">';
               }
               
               if (vShowSelect === 1 ) {
                   vLeftTable +='<td class="ggtaskCheckbox"><input type="checkbox" id="taskChecked-'+vcurrDivID+'" name="taskChecked" onclick="g.selectItem(this,\''+vcurrDivID+'\');"  '+ (JSGantt.scope.selectedItems[vcurrDivID] ? ' checked="checked">'
                   : '>')+'<label for="taskChecked-'+vcurrDivID+'"></label></td>';
                }
               
               
               vLeftTable += '  <td class="ggTaskTitle ggCol'+colNumber+'"><span style="color: #aaaaaa">';

               for ( var j = 1; j < vTaskList[i].getLevel(); j++) {
                  vLeftTable += '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;';
               }

               vLeftTable += '</span>';

               if (vTaskList[i].getGroup()) {
                  if (vTaskList[i].getOpen() == 1){
                     vLeftTable += '<div id="group_' + vcurrDivID + '" class="gicon ggroup-expanded" onclick="JSGantt.folder(\'' + vcurrDivID + '\',' + vGanttVar + ');' + vGanttVar + '.DrawDependencies();"></div>';
                  } else {
                     vLeftTable += '<div id="group_' + vcurrDivID + '" class="gicon ggroup-collapsed" onclick="JSGantt.folder(\'' + vcurrDivID + '\',' + vGanttVar + ');' + vGanttVar + '.DrawDependencies();"></div>';
                  }
               } else {

                  vLeftTable += '<span style="color: #000000; font-weight:bold; font-size: 12px;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>';
               }

               vLeftTable += '<span class="task-title" > ' + vTaskList[i].getName() + '</span></td>';

               if (vShowRes === 1 ) {
                  vLeftTable += '  <td class="ggCol'+colNumber+'"  ><nobr>' + vTaskList[i].getResource() + '</nobr></td>';
               }
               if (vShowDur === 1) {
                  vLeftTable += '  <td class="ggCol'+colNumber+'"  ><nobr>' + vTaskList[i].getDuration(vFormat) + '</nobr></td>';
               }
               if (vShowComp === 1) {
               
                  vLeftTable += '  <td class="ggCol'+colNumber+'"  ><nobr>' + vTaskList[i].getCompStr() + '</nobr></td>';
               }
               if (vShowStartDate === 1) {
                  vLeftTable += '  <td class="ggCol'+colNumber+'"  ><nobr>' + JSGantt.formatDateStr(vTaskList[i].getStart(),
                        vDateDisplayFormat) + '</nobr></td>';
               }
               if (vShowEndDate === 1) {
                  vLeftTable += '  <td class="ggCol'+colNumber+'"  ><nobr>' + JSGantt.formatDateStr(vTaskList[i].getEnd(),
                        vDateDisplayFormat) + '</nobr></td>';
               }

               vLeftTable += '</tr>';

            }

            // DRAW the date format selector at bottom left. Another potential
            // GanttChart parameter to hide/show this selector
            vLeftTable += '</td></tr>';

            vLeftTable += '</tbody></table></td>';

            vMainTable += vLeftTable + "</div>";

            // Draw the Chart Rows
            vRightTable = '<div class="scroll yui-u" id="rightside">' + '<table style="width: ' + vChartWidth + 'px;" cellSpacing="0" cellPadding="0" border="0">' + '<thead><tr class="gheader">';

            vTmpDate.setFullYear(vMinDate.getFullYear(), vMinDate.getMonth(), vMinDate.getDate());
            vTmpDate.setHours(0);
            vTmpDate.setMinutes(0);

            // Major Date Header
            while (vTmpDate.getTime() <= vMaxDate.getTime()) {
               vStr = vTmpDate.getFullYear() + '';
               vStr = vStr.substring(0, 4);

               if (vFormat == 'minute') {
                  vRightTable += '<th class="gdatehead"   colspan="60">';
                  vRightTable += JSGantt.formatDateStr(vTmpDate, vDateDisplayFormat) + ' ' + vTmpDate.getHours() + ':00 -' + vTmpDate
                        .getHours() + ':59 </th>';
                  vTmpDate.setHours(vTmpDate.getHours() + 1);
               }

               if (vFormat == 'hour') {
                  vRightTable += '<th class="gdatehead"   colspan="24">';
                  vRightTable += JSGantt.formatDateStr(vTmpDate, vDateDisplayFormat) + '</th>';
                  vTmpDate.setDate(vTmpDate.getDate() + 1);
               }

               if (vFormat == 'day') {
                   
                  vRightTable += '<th class="gdatehead"   colspan="7"><center>' + vTmpDate.getDate() + ' - ';
                  vTmpDate.setDate(vTmpDate.getDate() + 6);
                  vRightTable += JSGantt.formatDateStr(vTmpDate, vDateDisplayFormat) + '</center></th>';
                  vTmpDate.setDate(vTmpDate.getDate() + 1);
               } else if (vFormat == 'week') {
                  vRightTable += '<th class="gdatehead"  width=' + vColWidth + 'px><center>' + vStr + '</center></th>';
                  vTmpDate.setDate(vTmpDate.getDate() + 7);
               } else if (vFormat == 'month') {
                  vRightTable += '<th class="gdatehead"  width=' + vColWidth + 'px><center>' + vStr + '</center></th>';
                  vTmpDate.setDate(vTmpDate.getDate() + 1);
                  while (vTmpDate.getDate() > 1) {
                     vTmpDate.setDate(vTmpDate.getDate() + 1);
                  }
               } else if (vFormat == 'quarter') {
                  vRightTable += '<th class="gdatehead"   width=' + vColWidth + 'px><center>' + vStr + '</center></th>';
                  vTmpDate.setDate(vTmpDate.getDate() + 81);
                  while (vTmpDate.getDate() > 1) {
                     vTmpDate.setDate(vTmpDate.getDate() + 1);
                  }
               }

            }

            vRightTable += '</tr><tr class="gheader">';

            // Minor Date header and Cell Rows
            vTmpDate.setFullYear(vMinDate.getFullYear(), vMinDate.getMonth(), vMinDate.getDate());
            vNxtDate.setFullYear(vMinDate.getFullYear(), vMinDate.getMonth(), vMinDate.getDate());
            vNumCols = 0;
            
          

            while (vTmpDate.getTime() <= vMaxDate.getTime()) {
                var cssClass = "ghead" , vHeader = ""; 
                
               if (vFormat == 'minute') {

                  if (vTmpDate.getMinutes() === 0) {
                      cssClass = "ghead current";
                     vCurrPosY = vColWidth * vNumCols;
                  } 
                
                  vHeader = vTmpDate.getMinutes();
                  vTmpDate.setMinutes(vTmpDate.getMinutes() + 1);
               }

               else if (vFormat == 'hour') {

                  if (vTmpDate.getHours() === 0) {
                      cssClass = "ghead current";
                     vCurrPosY = vColWidth * vNumCols;
                  }
                  vHeader = vTmpDate.getHours();
                  vTmpDate.setHours(vTmpDate.getHours() + 1);
               }
               else if (vFormat == 'day') {
                   
                   if (vTmpDate.getDay() % 6 === 0) {
                       cssClass = "gheadwkend";
                   }
                   
                  if (JSGantt.formatDateStr(vCurrDate, 'shortDate') == JSGantt.formatDateStr(vTmpDate, 'shortDate')) {
                      cssClass += " current";
                     vCurrPosY = vColWidth * vNumCols;
                  }
                  
                  vHeader = vTmpDate.getDate();
                  vTmpDate.setDate(vTmpDate.getDate() + 1);

               }

               else if (vFormat == 'week') {

                  vNxtDate.setDate(vNxtDate.getDate() + 7);
                  if (vCurrDate >= vTmpDate && vCurrDate < vNxtDate) {
                      cssClass = "ghead current";
                     vCurrPosY = vColWidth * vNumCols;
                  }
                  vHeader = vTmpDate.getDate() + '/' + (vTmpDate.getMonth() + 1);
                  vTmpDate.setDate(vTmpDate.getDate() + 7);

               }
               else if (vFormat == 'month') {

                  vNxtDate.setFullYear(vTmpDate.getFullYear(), vTmpDate.getMonth(), vMonthDaysArr[vTmpDate.getMonth()]);
                  if (vCurrDate >= vTmpDate && vCurrDate < vNxtDate) {
                      cssClass = "ghead current";
                     vCurrPosY = vColWidth * vNumCols;
                  }
                  vHeader = vMonthArr[vTmpDate.getMonth()].substr(0, 3);
                  vTmpDate.setDate(vTmpDate.getDate() + 1);

                  while (vTmpDate.getDate() > 1) {
                     vTmpDate.setDate(vTmpDate.getDate() + 1);
                  }

               }

               else if (vFormat === 'quarter') {

                  vNxtDate.setDate(vNxtDate.getDate() + 122);
                  if (vTmpDate.getMonth() === 0 || vTmpDate.getMonth() === 1 || vTmpDate.getMonth() === 2)
                     vNxtDate.setFullYear(vTmpDate.getFullYear(), 2, 31);
                  else if (vTmpDate.getMonth() === 3 || vTmpDate.getMonth() === 4 || vTmpDate.getMonth() === 5)
                     vNxtDate.setFullYear(vTmpDate.getFullYear(), 5, 30);
                  else if (vTmpDate.getMonth() === 6 || vTmpDate.getMonth() === 7 || vTmpDate.getMonth() === 8)
                     vNxtDate.setFullYear(vTmpDate.getFullYear(), 8, 30);
                  else if (vTmpDate.getMonth() === 9 || vTmpDate.getMonth() === 10 || vTmpDate.getMonth() === 11)
                     vNxtDate.setFullYear(vTmpDate.getFullYear(), 11, 31);

                  if (vCurrDate >= vTmpDate && vCurrDate < vNxtDate) {
                      cssClass = "ghead current";
                     vCurrPosY = vColWidth * vNumCols;
                  }

                  vHeader = JSGantt.msg("jsgantt.quarter").substr(0, 3)+'. ' + vQuarterArr[vTmpDate.getMonth()];
                  vTmpDate.setDate(vTmpDate.getDate() + 81);
                  while (vTmpDate.getDate() > 1) {
                     vTmpDate.setDate(vTmpDate.getDate() + 1);
                  }

               }
               
               vDateRowStr += this.renderDateHeaderCell(cssClass, vColWidth, vHeader);
               vItemRowStr += this.renderDateCell(cssClass,vColWidth);
               vNumCols++;

            }

            vRightTable += vDateRowStr + '</tr>';
            vRightTable += '</thead></table>';

            // Draw each row

            for (i = 0; i < vTaskList.length; i++)

            {

               vTmpDate.setFullYear(vMinDate.getFullYear(), vMinDate.getMonth(), vMinDate.getDate());
               vTaskStart = vTaskList[i].getStart();
               vTaskEnd = vTaskList[i].getEnd();

               vcurrDivID = vTaskList[i].getID();

               // vNumUnits = Math.ceil((vTaskList[i].getEnd() -
               // vTaskList[i].getStart()) / (24 * 60 * 60 * 1000)) + 1;
               vNumUnits = (vTaskList[i].getEnd() - vTaskList[i].getStart()) / (24 * 60 * 60 * 1000) + 1;
               if (vFormat == 'hour') {
                  vNumUnits = (vTaskList[i].getEnd() - vTaskList[i].getStart()) / (60 * 1000) + 1;
               } else if (vFormat == 'minute') {
                  vNumUnits = (vTaskList[i].getEnd() - vTaskList[i].getStart()) / (60 * 1000) + 1;
               }

               if (vTaskList[i].getVisible() === 0) {
                  vRightTable += '<div id="childgrid_' + vcurrDivID + '" style="position:relative;" class="hidden" >';
               } else {
                  vRightTable += '<div id="childgrid_' + vcurrDivID + '" style="position:relative">';
               }

               if (vFormat == 'minute') {
                   vTaskRight = (vTaskList[i].getEnd().getTime() - vTaskList[i].getStart().getTime()) / (60 * 1000) + 1 / vColUnit;
                   vTaskLeft = Math.ceil((vTaskList[i].getStart().getTime() - vMinDate.getTime()) / (60 * 1000));
                } else if (vFormat == 'hour') {
                   vTaskRight = (vTaskList[i].getEnd().getTime() - vTaskList[i].getStart().getTime()) / (60 * 60 * 1000) + 1 / vColUnit;
                   vTaskLeft = (vTaskList[i].getStart().getTime() - vMinDate.getTime()) / (60 * 60 * 1000);
                } else {
                   vTaskRight = (vTaskList[i].getEnd().getTime() - vTaskList[i].getStart().getTime()) / (24 * 60 * 60 * 1000) + 1 / vColUnit;
                   vTaskLeft = Math
                         .ceil((vTaskList[i].getStart().getTime() - vMinDate.getTime()) / (24 * 60 * 60 * 1000));
                }
               
               if (g.getCaptionType()) {
                   vCaptionStr = '';
                   switch (g.getCaptionType()) {
                      case 'Caption':
                         vCaptionStr = vTaskList[i].getCaption();
                         break;
                      case 'Resource':
                         vCaptionStr = vTaskList[i].getResource();
                         break;
                      case 'Duration':
                         vCaptionStr = vTaskList[i].getDuration(vFormat);
                         break;
                      case 'Complete':
                         vCaptionStr = vTaskList[i].getCompStr();
                         break;
                   }
               }
               
               
               var vClass = "row";
               if (vTaskList[i].getMile()) {
                   vClass="mile";
               } else  if (vTaskList[i].getGroup()) {
                   vClass="group"; 
               }
               
               var vLineColorStyle = vTaskList[i].getLineColor()? 'style="background-color:' 
               + vTaskList[i].getLineColor() + '"' :"";
   
               vRightTable += '<div><table style="width: ' + vChartWidth + 'px;"><tbody class="yui-dt-data">' 
                           + '<tr id="childrow_' + vcurrDivID + '" '+vLineColorStyle+' class="g'+ vClass +'" onMouseover="g.mouseOver(this,\'' 
                           + vcurrDivID + '\',\'right\',\''+vClass+'\')" onMouseout="g.mouseOut(this,\'' 
                           + vcurrDivID + '\',\'right\',\''+vClass+'\')">' 
                           + vItemRowStr + '</tr></tbody></table></div>';
    
           
               if (vTaskList[i].getMile()) {

                  // Build date string for Title
                  vDateRowStr = JSGantt.formatDateStr(vTaskStart, vDateDisplayFormat);

                  vRightTable += this.renderMile(vcurrDivID,vTaskLeft,vTaskRight,vDayWidth,vColWidth,vDateRowStr, vCaptionStr,vTaskList[i], vFormat);
                

               } else {

                  // Build date string for Title
                  vDateRowStr = JSGantt.formatDateStr(vTaskStart, vDateDisplayFormat) + ' - ' + JSGantt.formatDateStr(
                        vTaskEnd, vDateDisplayFormat);

                  // Draw Group Bar which has outer div with inner group div and
                  // several small divs to left and right to create angled-end
                  // indicators
                  if (vTaskList[i].getGroup()) {
                  
                      var m = 0, milesDiv = "", vTaskLeftCustom = 0, vDateRowStrCustom = "";	
                      
					  for (m = i+1; m < vTaskList.length; m++){	
 							if (!vTaskList[m].getGroup() && vTaskList[m].getMile() && vTaskList[m].getParent() == vcurrDivID) {
 							   if (vFormat == 'minute') {
 							      vTaskLeftCustom = Math.ceil((vTaskList[m].getStart().getTime() - vMinDate.getTime()) / (60 * 1000));
 				                } else if (vFormat == 'hour') {
 				                   vTaskLeftCustom = (vTaskList[m].getStart().getTime() - vMinDate.getTime()) / (60 * 60 * 1000);
 				                } else {
 				                   vTaskLeftCustom = Math
 				                         .ceil((vTaskList[m].getStart().getTime() - vMinDate.getTime()) / (24 * 60 * 60 * 1000));
 				                }  
 							  vDateRowStrCustom = JSGantt.formatDateStr(vTaskList[m].getStart(), vDateDisplayFormat);
 	                          
 							 if(vDayWidth < 6){
 							    vTaskLeftCustom = vTaskLeftCustom -  Math.ceil(6/vDayWidth);
 					          }
 							  
 	                          milesDiv  += '<div style="position:absolute;top:5px; left:'+
 	                              Math.ceil(vTaskLeftCustom * (vDayWidth) + 1)+'px; z-index:1; background-color:#' 
 	                               + vTaskList[m].getColor() + ';" title="'+vDateRowStrCustom+'" class="milestone" >&nbsp;</div>'; 										   
 							}
						}
      
                  
                       vRightTable += milesDiv + this.renderGroup(vcurrDivID,vTaskLeft,vTaskRight,vDayWidth, vColWidth, vDateRowStr, vCaptionStr ,vTaskList[i], vFormat );

                  } else {
                     
                     // Draw Task Bar which has outer div with enclosed colored
                     // bar
                     // div, and opaque completion div
                     vRightTable += this.renderTask(vcurrDivID,vTaskLeft,vTaskRight,vDayWidth, vColWidth, vDateRowStr, vCaptionStr,vTaskList[i], vFormat );

                  }


               }
               vRightTable += '</div></div>';

            }

            
            vMainTable += vRightTable + '</div></div>';

            vDiv.innerHTML = vMainTable;

         }

            
//         this.itemSelectButton = Alfresco.util.createYUIButton(JSGantt.scope, "JSGantt-itemSelect-button", function (sType, aArgs, p_obj)   {
//             var domEvent = aArgs[0], eventTarget = aArgs[1];
//                 alert("pwet");
//             
//                 JSGantt.scope.selectItems(Alfresco.util.findEventClass(eventTarget));
//                 Event.preventDefault(domEvent);
//         }, {
//              type : "menu",
//              menu : "JSGantt-itemSelect-menu",
//              disabled : false
//         });
//
//         // Enable item select menu
//         Dom.removeClass(JSGantt.scope.id+"-JSGantt-itemSelect-div", "hidden");


            
         this.scrollToY(vCurrPosY,vDiv);
         
         } catch (e){
             alert(e);
         }

      }; 
      
      
      this.renderMile = function(vcurrDivID,vTaskLeft,vTaskRight,vDayWidth,vColWidth,vDateRowStr,vCaptionStr,vTask, vFormat){
          if(vDayWidth < 6){
              vTaskLeft = vTaskLeft -  Math.ceil(6/vDayWidth);
          }
          
          return '<div id="bardiv_' + vcurrDivID + '" style="position:absolute; top:3px; left:' + Math
          .ceil(vTaskLeft * (vDayWidth) + 1)  + 'px; height: 16px; width:266px; overflow:hidden;">' + '  <div id="taskbar_' + vcurrDivID + '" title="'+vDateRowStr 
          + '"  class="milestone ' + (vTask.getCompVal() < 100 ? "" : "completed") + '" style="background-color:#' + vTask.getColor() + ';">&nbsp;</div>'
          + (( vCaptionStr!=null && vCaptionStr.length>0)?'<div class="gcaption" style="top:0px; left:20px">' + vCaptionStr + '</div>': '');
         
      };
      
      this.renderTask = function(vcurrDivID,vTaskLeft,vTaskRight,vDayWidth,vColWidth, vDateRowStr,vCaptionStr,vTask, vFormat){
         return  '<div id="bardiv_' + vcurrDivID + '" style="position:absolute; top:4px; left:' 
          + Math.ceil(vTaskLeft * (vDayWidth) - 1) + 'px; height:18px; width:' 
          + Math.ceil((vTaskRight) * (vDayWidth) - 1) + 'px">' + '<div id="taskbar_' + vcurrDivID + '" title="' + vDateRowStr 
          + '" class="gtask" style="background-color:#' + vTask.getColor() + '; width:' + Math.ceil((vTaskRight) * (vDayWidth) - 1) + 'px;"  >' 
          + '<div class="gcomplete" style="width:' + vTask.getCompStr() + ';">' + '</div>' + '</div>'
          + (( vCaptionStr!=null && vCaptionStr.length>0)?'<div class="gcaption" style="top:-3px; left:'
          +  (Math.ceil((vTaskRight) * (vDayWidth) - 1) + 6) + 'px">' + vCaptionStr + '</div>': '');
         
      };
      
      this.renderGroup = function(vcurrDivID,vTaskLeft,vTaskRight,vDayWidth,vColWidth, vDateRowStr,vCaptionStr ,vTask, vFormat){
          return '<div id="bardiv_' + vcurrDivID + '" style="position:absolute; top:8px; left:' 
          + Math.ceil(vTaskLeft * (vDayWidth) - 1) + 'px; height: 7px; width:' 
          + Math.ceil((vTaskRight) * (vDayWidth) - 1) + 'px">' + '<div id="taskbar_' + vcurrDivID + '" title="' + vDateRowStr + '" class="ggtask" style="width:' 
          + Math.ceil((vTaskRight) * (vDayWidth) - 1) + 'px;">' 
          + '<div class="ggcomplete" style="width:' + vTask.getCompStr() + '" ></div></div>'
          + (( vCaptionStr!=null && vCaptionStr.length>0)?'<div class="gcaption" style="top:-3px; left:'
          +  (Math.ceil((vTaskRight) * (vDayWidth) - 1) + 6) + 'px">' + vCaptionStr + '</div>': '');
      };
      
      
      this.renderDateHeaderCell = function(cssClass, vColWidth, vHeader){
          return  '<th class="'+cssClass+'"  ><div style="width: ' + vColWidth + 'px">' + vHeader + '</div></th>';
      };
      
      this.renderDateCell = function(cssClass, vColWidth){ 
          return  '<td class="'+cssClass+'"  ><div style="width: ' + vColWidth + 'px">&nbsp;&nbsp;</div></td>';
          
      };
      
      /**
       * Select nodeRef when checkbox click
       */
      this.selectItem = function(input,id){

          JSGantt.scope.selectedItems[id] = input.checked;
          YAHOO.Bubbling.fire(JSGantt.scope.scopeId+"selectedItemsChanged");
      };

      /**
       * Mouseover behaviour for gantt row
       * 
       * @method mouseOver
       * @return {Void}
       */
      this.mouseOver = function(pObj, pID, pPos, pType) {
         if (pPos == 'right')
            vcurrDivID = 'child_' + pID;
         else
            vcurrDivID = 'childrow_' + pID;

         Dom.addClass(pObj, "highlight");
         Dom.addClass(vcurrDivID, "highlight");
      };

      /**
       * Mouseout behaviour for gantt row
       * 
       * @method mouseOut
       * @return {Void}
       */
      this.mouseOut = function(pObj, pID, pPos, pType) {
         if (pPos == 'right')
            vcurrDivID = 'child_' + pID;
         else
            vcurrDivID = 'childrow_' + pID;

         Dom.removeClass(pObj, "highlight");
         Dom.removeClass(vcurrDivID, "highlight");

      };

   }; // GanttChart

   /**
    * @class
    */

   /**
    * Recursively process task tree ... set min, max dates of parent tasks and identfy task level.
    * 
    * @method processRows
    * @param pList
    *            {Array} - Array of TaskItem Objects
    * @param pID
    *            {Number} - task ID
    * @param pRow
    *            {Number} - Row in chart
    * @param pLevel
    *            {Number} - Current tree level
    * @param pOpen
    *            {Boolean}
    * @return void
    */
   JSGantt.processRows = function(pList, pID, pRow, pLevel, pOpen) {

      var vMinDate = new Date();
      var vMaxDate = new Date();
      var vMinSet = 0;
      var vMaxSet = 0;
      var vList = pList;
      var vLevel = pLevel;
      var i = 0;
      var vNumKid = 0;
      var vCompSum = 0;
      var vVisible = pOpen;

      for (i = 0; i < pList.length; i++) {
         if (pList[i].getParent() == pID) {
            vVisible = pOpen;
            pList[i].setVisible(vVisible);
            if (vVisible == 1 && pList[i].getOpen() === 0) {
               vVisible = 0;
            }

            pList[i].setLevel(vLevel);
            vNumKid++;

            if (pList[i].getGroup() === 1) {
               JSGantt.processRows(vList, pList[i].getID(), i, vLevel + 1, vVisible);
            }

            if (vMinSet === 0 || pList[i].getStart() < vMinDate) {
               vMinDate = pList[i].getStart();
               vMinSet = 1;
            }

            if (vMaxSet === 0 || pList[i].getEnd() > vMaxDate) {
               vMaxDate = pList[i].getEnd();
               vMaxSet = 1;
            }

            vCompSum += pList[i].getCompVal();

         }
      }

      if (pRow >= 0 && ! pList[pRow].getMile()) {
         pList[pRow].setStart(vMinDate);
         pList[pRow].setEnd(vMaxDate);
         pList[pRow].setNumKid(vNumKid);
        // pList[pRow].setCompVal(Math.ceil(vCompSum / vNumKid));
      }

   };

   /**
    * Determine the minimum date of all tasks and set lower bound based on format
    * 
    * @method getMinDate
    * @param pList
    *            {Array} - Array of TaskItem Objects
    * @param pFormat
    *            {String} - current format (minute,hour,day...)
    * @return {Datetime}
    */
   JSGantt.getMinDate = function getMinDate(pList, pFormat) {

      var vDate = new Date(), vStart;

      vDate.setFullYear(pList[0].getStart().getFullYear(), pList[0].getStart().getMonth(), pList[0].getStart()
            .getDate());

      // Parse all Task End dates to find min
      for ( var i = 0; i < pList.length; i++) {
         vStart = pList[i].getStart();
         if (vStart.getTime() < vDate.getTime())
            vDate.setFullYear(pList[i].getStart().getFullYear(), pList[i].getStart().getMonth(), pList[i].getStart()
                  .getDate());
      }

      if (pFormat == 'minute') {
         vDate.setHours(0);
         vDate.setMinutes(0);
      } else if (pFormat == 'hour') {
         vDate.setHours(0);
         vDate.setMinutes(0);
      }
      // Adjust min date to specific format boundaries (first of week or first
      // of
      // month)
      else if (pFormat == 'day') {
         vDate.setDate(vDate.getDate() - 1);
         while (vDate.getDay() % 7 > 0) {
            vDate.setDate(vDate.getDate() - 1);
         }

      }

      else if (pFormat == 'week') {
         vDate.setDate(vDate.getDate() - 7);
         while (vDate.getDay() % 7 > 0) {
            vDate.setDate(vDate.getDate() - 1);
         }

      }

      else if (pFormat == 'month') {
         while (vDate.getDate() > 1) {
            vDate.setDate(vDate.getDate() - 1);
         }
      }

      else if (pFormat == 'quarter') {
         if (vDate.getMonth() === 0 || vDate.getMonth() === 1 || vDate.getMonth() === 2) {
            vDate.setFullYear(vDate.getFullYear(), 0, 1);
         } else if (vDate.getMonth() === 3 || vDate.getMonth() === 4 || vDate.getMonth() === 5) {
            vDate.setFullYear(vDate.getFullYear(), 3, 1);
         } else if (vDate.getMonth() === 6 || vDate.getMonth() === 7 || vDate.getMonth() === 8) {
            vDate.setFullYear(vDate.getFullYear(), 6, 1);
         } else if (vDate.getMonth() === 9 || vDate.getMonth() === 10 || vDate.getMonth() === 11) {
            vDate.setFullYear(vDate.getFullYear(), 9, 1);
         }

      }

      return (vDate);

   };

   /**
    * Used to determine the minimum date of all tasks and set lower bound based on format
    * 
    * @method getMaxDate
    * @param pList
    *            {Array} - Array of TaskItem Objects
    * @param pFormat
    *            {String} - current format (minute,hour,day...)
    * @return {Datetime}
    */
   JSGantt.getMaxDate = function(pList, pFormat) {
      var vDate = new Date();

      vDate.setFullYear(pList[0].getEnd().getFullYear(), pList[0].getEnd().getMonth(), pList[0].getEnd().getDate());

      // Parse all Task End dates to find max
      for ( var i = 0; i < pList.length; i++) {
         if (pList[i].getEnd().getTime() > vDate.getTime()) {
            // vDate.setFullYear(pList[0].getEnd().getFullYear(),
            // pList[0].getEnd().getMonth(), pList[0].getEnd().getDate());
            vDate.setTime(pList[i].getEnd().getTime());
         }
      }
      
      

      if (pFormat == 'minute') {
         vDate.setHours(vDate.getHours() + 1);
         vDate.setMinutes(59);
      }

      if (pFormat == 'hour') {
         vDate.setHours(vDate.getHours() + 2);
      }

      // Adjust max date to specific format boundaries (end of week or end of
      // month)
      if (pFormat == 'day') {
    	  
    	  
        vDate.setDate(vDate.getDate() + 1);
         
   	  	if(vDate.getDay() == 0){
        	vDate.setDate(vDate.getDate() + 1);
        } 
   	  
         while (vDate.getDay() % 6 > 0) {
            vDate.setDate(vDate.getDate() + 1);
         }

      }

      if (pFormat == 'week') {
         // For weeks, what is the last logical boundary?
         vDate.setDate(vDate.getDate() + 11);

         while (vDate.getDay() % 6 > 0) {
            vDate.setDate(vDate.getDate() + 1);
         }

      }

      // Set to last day of current Month
      if (pFormat == 'month') {
         while (vDate.getDay() > 1) {
            vDate.setDate(vDate.getDate() + 1);
         }

         vDate.setDate(vDate.getDate() - 1);
      }

      // Set to last day of current Quarter
      if (pFormat == 'quarter') {
         if (vDate.getMonth() === 0 || vDate.getMonth() === 1 || vDate.getMonth() === 2)
            vDate.setFullYear(vDate.getFullYear(), 2, 31);
         else if (vDate.getMonth() === 3 || vDate.getMonth() === 4 || vDate.getMonth() === 5)
            vDate.setFullYear(vDate.getFullYear(), 5, 30);
         else if (vDate.getMonth() === 6 || vDate.getMonth() === 7 || vDate.getMonth() === 8)
            vDate.setFullYear(vDate.getFullYear(), 8, 30);
         else if (vDate.getMonth() === 9 || vDate.getMonth() === 10 || vDate.getMonth() === 11)
            vDate.setFullYear(vDate.getFullYear(), 11, 31);

      }

      return (vDate);

   };

   /**
    * Change display format of current gantt chart
    * 
    * @method changeFormat
    * @param pFormat
    *            {String} - Current format (minute,hour,day...)
    * @param ganttObj
    *            {GanttChart} - the gantt object
    * @return {void}
    */
   JSGantt.changeFormat = function(pFormat, ganttObj) {

      if (ganttObj) {
         ganttObj.setFormat(pFormat);
         ganttObj.DrawDependencies();   
      } else {
         alert('Chart undefined');
      }
   };

   /**
    * Open/Close and hide/show children of specified task
    * 
    * @method folder
    * @param pID
    *            {Number} - Task ID
    * @param ganttObj
    *            {GanttChart} - the gantt object
    * @return {void}
    */
   JSGantt.folder = function(pID, ganttObj) {

      var vList = ganttObj.getList();

      for ( var i = 0; i < vList.length; i++) {
         if (vList[i].getID() == pID) {

            if (vList[i].getOpen() == 1) {
               vList[i].setOpen(0);
               JSGantt.toogleDiv(pID, ganttObj,0);
               Dom.removeClass('group_' + pID, "ggroup-expanded");
               Dom.addClass('group_' + pID, "ggroup-collapsed");

            } else {

               vList[i].setOpen(1);
               JSGantt.toogleDiv(pID, ganttObj,1);
               Dom.removeClass('group_' + pID, "ggroup-collapsed");
               Dom.addClass('group_' + pID, "ggroup-expanded");

            }
            

         }
      }
   };

   /**
    * Toogle children of a task
    * 
    * @method hide
    * @param pID
    *            {Number} - Task ID
    * @param ganttObj
    *            {GanttChart} - the gantt object
    * @param pShow
    *            {pShow} - 0/1 hide or show div
    * @return {void}
    */
   JSGantt.toogleDiv = function(pID, ganttObj, pShow) {
      var vList = ganttObj.getList();
      var vCurrID = 0;

      for ( var i = 0; i < vList.length; i++) {
         if (vList[i].getParent() == pID) {
            vCurrID = vList[i].getID();
            if(pShow == 0){
                Dom.addClass('child_' + vCurrID, "hidden");
                Dom.addClass('childgrid_' + vCurrID, "hidden");
            } else {
                Dom.removeClass('child_' + vCurrID, "hidden");
                Dom.removeClass('childgrid_' + vCurrID, "hidden");
            }
            vList[i].setVisible(pShow);
            if (vList[i].getGroup() == 1) {
                JSGantt.toogleDiv(vCurrID, ganttObj,pShow);
            }
         }

      }
   };


   /**
    * Parse dates based on gantt date format setting as defined in JSGantt.GanttChart.setDateInputFormat()
    * 
    * @method parseDateStr
    * @param pDateStr
    *            {String} - A string that contains the date (i.e. "01/01/09")
    * @param pFormatStr
    *            {String} - the date format (mm/dd/yyyy,dd/mm/yyyy,yyyy-mm-dd)
    * @return {Datetime}
    */
   JSGantt.parseDateStr = function(pDateStr, pFormatStr) {
      if (pDateStr === null || pDateStr.length < 1 ) {
         return new Date();
      }
      if (pDateStr instanceof Date) {
         return pDateStr;
      }

      var ret = Alfresco.util.fromISO8601(pDateStr);
      if (ret === null) {
         return new Date();
      }

      return ret;
   };

   /**
    * Display a formatted date based on gantt date format setting as defined in
    * JSGantt.GanttChart.setDateDisplayFormat()
    * 
    * @method formatDateStr
    * @param pDate
    *            {Date} - A javascript date object
    * @param pFormatStr
    *            {String} - the date format (mm/dd/yyyy,dd/mm/yyyy,yyyy-mm-dd...)
    * @return {String}
    */
   JSGantt.formatDateStr = function(pDate, pFormatStr) {

      return Alfresco.util.formatDate(pDate, pFormatStr);

   };

})();

import { EntityVersionService } from './../../../../api/entity-version.service';
import { Component, Inject, ViewChild, ElementRef, OnInit, AfterViewInit } from "@angular/core";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { Entity } from "../../../../model/Entity";
import { NodeEntry, Node } from 'alfresco-js-api';
import { DownloadZipService } from '@alfresco/adf-core';


@Component({
  selector: "app-entity-version-history-popup",
  templateUrl: "./entity-version-history-popup.component.html",
  styleUrls: ["./entity-version-history-popup.component.scss"]
})
export class EntityVersionHistoryPopupComponent implements OnInit {


  entity: Entity;

  versions: any[];

  node: NodeEntry;

  entityLogoUrl: string;

  colors = [ [ 1.0, 0.0, 0.0 ], [ 1.0, 1.0, 0.0 ], [ 0.0, 1.0, 0.0 ], [ 0.0, 1.0, 1.0 ],
   [ 0.0, 0.0, 1.0 ], [ 1.0, 0.0, 1.0 ], [ 0.0, 0.0, 0.0 ] ];
  line_width = 5.0;
  dot_radius = 5;
  ctx: any;

  @ViewChild("graphCanvas") graphCanvas: ElementRef;
  @ViewChild("graphContent", {read: ElementRef}) graphContent: ElementRef;

  constructor(public dialogRef: MatDialogRef<EntityVersionHistoryPopupComponent>,
    private entityVersionService: EntityVersionService,
    private downloadZipService: DownloadZipService,
    @Inject(MAT_DIALOG_DATA) public data: any) {}


  ngOnInit(): void {
    this.entity = this.data.entity;
    this.entityLogoUrl = this.data.entityLogoUrl;
    this.node = ({entry: this.entity as Node});

    this.entityVersionService.getEntityHistory(this.entity.id)
      .then(
        (ret) => {
          this.versions = ret;
        },
        (error) => {
          console.log("Error, "+error);
        }
        );


  }

  onClickCancel(): void {
    this.dialogRef.close();
  }

  onShowGraph(){
    this.renderGraph(100);
  }

  isLoading(): boolean {
    return false;
  }

  downloadZip(id: string) {}

  renderGraph(canvasWidth): void{
    let canvas  = this.graphCanvas.nativeElement;
    if (canvas.getContext) {
      this.ctx = canvas.getContext('2d');

      let graphData = Object.assign([], this.versions);
      graphData.reverse();

         var nbCols = 0, columnsPos = {}, shouldBranch = false, columns = {};
         for ( var i in graphData) {
            var entityNodeRef = graphData[i].entityNodeRef, entityFromBranch = graphData[i].entityFromBranch;

            var test = columnsPos[entityNodeRef];
            if (!test && test != 0) {
               //reorder cols
               if(entityFromBranch){
                  for(var j in columnsPos){
                     if(columnsPos[j] > columnsPos[entityFromBranch]){
                        columnsPos[j] = columnsPos[j] + 1;
                     }
                  }
                  columnsPos[graphData[i].entityNodeRef] = columnsPos[entityFromBranch]+1;
               } else {
                  columnsPos[graphData[i].entityNodeRef] = nbCols;
               }
               nbCols++;
            }

         }

         var edge_pad = this.dot_radius + 2;
         var box_size = Math.min(18, Math.floor((canvasWidth - edge_pad * 2) / (nbCols)));
         var base_x = canvasWidth - edge_pad - 10;
         var prec = {}, extra = 15;
         nbCols = 0;
         var idx = graphData.length - 1;
         var yOffset = canvas.offsetTop - extra;

         for ( var i in graphData) {
            var listItems = this.graphContent.nativeElement.children;
            var item = listItems[idx - 1];
            var nextY = item ? item.offsetTop - yOffset : -yOffset;
            var rowY = listItems[idx].offsetTop - yOffset;

            var entityNodeRef = graphData[i].entityNodeRef, entityFromBranch = graphData[i].entityFromBranch;

            var test = columns[entityNodeRef];
            if (!test && test != 0) {
               columns[entityNodeRef] = nbCols++;
               shouldBranch = true;
            }

            for ( var j in columns) {
               var end = columnsPos[j], precNodeRef = (shouldBranch && j == entityNodeRef && entityFromBranch != null) ? entityFromBranch
                     : j, start = columnsPos[precNodeRef];

               this.setColor(end, 0.0, 0.65);
               let x = base_x - box_size * start;
               this.ctx.lineWidth = this.line_width;
               this.ctx.beginPath();

               if (start != end) {
                  var prevY =
                  listItems[prec[precNodeRef]] - yOffset - this.dot_radius;
                  this.ctx.moveTo(x, prevY);
                  var x2 = base_x - box_size * end;
                  var ymid = (rowY + prevY) / 2;
                  this.ctx.bezierCurveTo(x, ymid, x2, ymid, x2, rowY);
                  this.ctx.moveTo(x2, rowY);
                  this.ctx.lineTo(x2, nextY, 3);
               } else {
                  this.ctx.moveTo(x, rowY);
                  this.ctx.lineTo(x, nextY, 3);
               }
               this.ctx.stroke();
            }

            shouldBranch = false;
            let radius = this.dot_radius;
            let x = base_x - box_size * columnsPos[entityNodeRef];
            prec[entityNodeRef] = idx;

            this.ctx.beginPath();
            this.setColor(columnsPos[entityNodeRef], 0.25, 0.75);
            this.ctx.arc(x, rowY, radius, 0, Math.PI * 2, true);
            this.ctx.fill();
            idx--;
        }
    }
}

setColor(color, bg, fg) {
  var vColor = color % this.colors.length;
  var red = (this.colors[vColor][0] * fg) || bg;
  var green = (this.colors[vColor][1] * fg) || bg;
  var blue = (this.colors[vColor][2] * fg) || bg;
  red = Math.round(red * 255);
  green = Math.round(green * 255);
  blue = Math.round(blue * 255);
  var s = 'rgb(' + red + ', ' + green + ', ' + blue + ')';
  this.ctx.strokeStyle = s;
  this.ctx.fillStyle = s;
}


}

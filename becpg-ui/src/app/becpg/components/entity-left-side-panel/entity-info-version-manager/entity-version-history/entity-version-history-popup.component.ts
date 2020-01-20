import { EntityVersionService } from './../../../../api/entity-version.service';
import { DocumentListComponent } from '@alfresco/adf-content-services';
import { Component, Inject, OnInit } from "@angular/core";
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


  constructor(public dialogRef: MatDialogRef<EntityVersionHistoryPopupComponent>,
    private entityVersionService: EntityVersionService,
    private downloadZipService: DownloadZipService,
    @Inject(MAT_DIALOG_DATA) public data: any) {}


  ngOnInit(): void {
    this.entity = this.data.entity;
    this.entityLogoUrl = this.data.entityLogoUrl;
    this.node = {entry: this.entity};
    console.log(this.entityLogoUrl);

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

  isLoading(): boolean {
    return false;
  }

  downloadZip(id: string) {


}


}

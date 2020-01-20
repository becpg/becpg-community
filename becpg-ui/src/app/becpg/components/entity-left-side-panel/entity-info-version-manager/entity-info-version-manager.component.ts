import { MinimalNodeEntity, MinimalNodeEntryEntity } from 'alfresco-js-api';
import { Component, OnInit, Input } from '@angular/core';
import { Entity } from '../../../model/Entity';
import { MatDialog } from '@angular/material';
import { EntityVersionHistoryPopupComponent } from './entity-version-history/entity-version-history-popup.component';
import { EntityVersionService } from '../../../api/entity-version.service';
import { EntityApiService } from '../../../api/entity-api.service';

@Component({
  selector: 'app-entity-info-version-manager',
  templateUrl: './entity-info-version-manager.component.html',
  styleUrls: ['./entity-info-version-manager.component.scss']
})
export class EntityInfoVersionManagerComponent implements OnInit{

  @Input()
  entity: Entity;

  entityLogoUrl: string;

  versions: any[];

  branches: any[];


  selectedEntities: MinimalNodeEntity[] = [];


  constructor(private dialog: MatDialog,
    private entityApiService: EntityApiService,
    private entityVersionService: EntityVersionService) { }

  ngOnInit(): void{

    this.entityLogoUrl =  this.entityApiService.getEntityLogoUrl(this.entity.id);
    const node: MinimalNodeEntity = { entry: this.entity };
    this.selectedEntities = [node];

    this.entityVersionService.getEntityVersions(this.entity.id)
      .then(
        (success) => {

          this.versions = success.versions;
          this.branches = success.branches;

        },
        (error) => {
          console.log("Error, "+error);
        }
        );
  }

  onShowHistory(){
    const dialogRef = this.dialog.open(EntityVersionHistoryPopupComponent,
      {data:
        {
          entity: this.entity,
          branches: this.branches,
          entityLogoUrl: this.entityLogoUrl
        }
      });
  }


}

import { Component, OnInit, Input, Inject, ViewChild } from '@angular/core';
import { EntityApiService } from '../../api/entity-api.service';
import { ActivatedRoute } from '@angular/router';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import { MatSidenav } from '@angular/material';
import { EntityLogoUploadPopupComponent } from './entity-logo-upload-popup/entity-logo-upload-popup.component';


@Component({
  selector: 'app-entity-logo',
  templateUrl: './entity-logo.component.html',
  styleUrls: ['./entity-logo.component.scss']
})
export class EntityLogoComponent implements OnInit {

  entityLogoUrl: string;
  nodeId: string;
  @Input()
  public leftPane: MatSidenav;

  constructor(private route: ActivatedRoute,
              private entityApiService: EntityApiService,
              private dialog: MatDialog) {}

  ngOnInit() {
     this.nodeId = this.route.snapshot.params['id'];
     this.entityLogoUrl = this.entityApiService.getEntityLogoUrl(this.nodeId);
  }

  openUploadLogoDialog(): void {
    const dialogRef = this.dialog.open(EntityLogoUploadPopupComponent,
      {data: {nodeId: this.nodeId}});

    dialogRef.afterClosed().subscribe(result => {
      setTimeout(
        () => {
        this.ngOnInit();
        }, 2000);

    });
  }

  toggleSideLeftNavbar(){
    this.leftPane.toggle();
  }


}



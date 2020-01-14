import {Component, Inject} from '@angular/core';
import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import { EntityApiService } from '../../../api/entity-api.service';
import { ActivatedRoute } from '@angular/router';
import { HttpEventType } from '@angular/common/http';


@Component({
  selector: 'app-entity-logo-upload-popup',
  templateUrl: './entity-logo-upload-popup.component.html',
  styleUrls: ['./entity-logo-upload-popup.component.scss']
})
export class EntityLogoUploadPopupComponent {

  logoToUpload: File = null;
  loadingProgress: number = 0;
  disabled: boolean = false;

  constructor(
    public dialogRef: MatDialogRef<EntityLogoUploadPopupComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any, 
    private entityApiService: EntityApiService, 
    private route: ActivatedRoute) {}


  doUploadEntityLogo(files: FileList): void{
    this.disabled = true;
    this.logoToUpload = files.item(0);
    const nodeId = this.data.nodeId;
    this.entityApiService.uploadEntityLogo(this.logoToUpload, nodeId)
    .subscribe(
      (events) => {
        if(events.type == HttpEventType.UploadProgress){
          this.loadingProgress =  Math.round(events.loaded / events.total) * 100;
        }else if (events.type == HttpEventType.Response){
          this.loadingProgress = 100;
          this.onClickCancel();
        }
      }, 
      (error) => {
        console.log('Error: ' + JSON.stringify(error));
        this.loadingProgress = 100;
        this.onClickCancel();

      });
  }

  onClickCancel(): void {
    this.dialogRef.close();
  }
  

}


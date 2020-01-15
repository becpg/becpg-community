import { Component, OnInit } from '@angular/core';
import { baseHost, WidgetComponent, FormService } from '@alfresco/adf-core';
import { FormControl } from '@angular/forms';
import { Observable, from } from 'rxjs';
import { mergeMap, startWith } from 'rxjs/operators';
import { AlfrescoApiService } from '@alfresco/adf-core';

export interface AutocompleteResult {
  value: string;
  name: string;
  cssClass: string;
  metadatas: string;
}

export interface AutocompletePageResults {
  result: AutocompleteResult[];
  page: number;
  pageSize: number;
  fullListSize: number;
}

@Component({
  selector: 'app-autocomplete',
  templateUrl: './autocomplete.component.html',
  styleUrls: ['./autocomplete.component.scss']
})

export class AutocompleteComponent extends WidgetComponent implements OnInit {

  stateCtrl = new FormControl();
  autoCompleteResults: Observable<any[]>;

  multipleSelectMode = false;

  parentFieldHtmlId: string;
  showColor = false;
  showToolTip = false;
  showPage = true;
  saveTitle = true;
  urlParamsToPass: string;
  ds = '';



  constructor(private apiService: AlfrescoApiService, public formService: FormService) {
    super(formService);
  }

  private getResults(query: string): Observable<any[]> {
   
    return from(this.apiService.getInstance()
      .webScript.executeWebScript('GET', this.generateRequest(query), null, null, null, null)
      .then(ret => {
        return ret.result;
      }));

  }


  ngOnInit() {

    if (this.field.params.ds) {
      this.ds = this.field.params.ds;
    } else {
      this.ds = `becpg/autocomplete/targetassoc/associations/${this.field.params.endpointType}`;
    }

    if (this.field.params.endpointMany) {
      this.multipleSelectMode = true;
    }

    this.autoCompleteResults = this.stateCtrl.valueChanges
      .pipe(
        startWith(''),
        mergeMap(query => this.getResults(query))
      );
  }


  private generateRequest(sQuery): string {

    let q = '';

    if (this.multipleSelectMode && sQuery.indexOf('%2C%20') > 0) {
      const arrQuery = sQuery.split('%2C%20');
      sQuery = arrQuery[arrQuery.length - 1];
    }

    let oParentField: string = null;

    if (this.parentFieldHtmlId != null) {

      oParentField = this.getParentValue(this.parentFieldHtmlId);

    }

    if (oParentField != null) {
      q = `q=${sQuery}&parent=${oParentField}&page=1`;
    } else {
      q = `q=${sQuery}&page=1`;
    }

    if (this.urlParamsToPass != null) {
      q = q + '&' + this.urlParamsToPass;
    }

    if (this.ds.indexOf('?') > 0) {
      return this.ds +'&' + q;
    }
    return this.ds +'?' + q;

    
  }


  getParentValue(parentFieldHtmlId: string): string {
    throw new Error("Method not implemented.");


    //   formService.formFieldValueChanged.subscribe((e: FormFieldEvent) => {
    //     if (e.field.id === 'type') {
    //         const fields: FormFieldModel[] = e.form.getFormFields();
    //         const description = fields.find(f => f.id === 'description');
    //         if (description != null) {
    //             console.log(description);
    //             description.value = 'Type set to ' + e.field.value;
    //         }
    //     }
    // });
  }

  getColor(metadatas: any) {
    let color = null;
    for (const key in metadatas) {
      if (Object.prototype.hasOwnProperty.call(metadatas, key)) {
        const obj = metadatas[key];
        if (obj['key'] === 'color') {
          color = obj['value'];
        }
      }
    }
    return color;
  }

}

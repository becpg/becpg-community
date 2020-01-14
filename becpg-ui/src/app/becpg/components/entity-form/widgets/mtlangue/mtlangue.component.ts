import { Component, OnInit } from '@angular/core';
import { baseHost , WidgetComponent, FormService } from '@alfresco/adf-core';

@Component({
  selector: 'app-mtlangue',
  templateUrl: './mtlangue.component.html',
  styleUrls: ['./mtlangue.component.scss'],
  host: baseHost
})
export class MtlangueComponent extends WidgetComponent implements OnInit {

    mask: string;
    placeholder: string;
    isMaskReversed: boolean;

    constructor(public formService: FormService) {
        super(formService);
    }

    ngOnInit() {
        if (this.field.params) {
            this.mask = this.field.params['inputMask'];
            this.placeholder = this.field.params['inputMask'] && this.field.params['inputMaskPlaceholder'] ? this.field.params['inputMaskPlaceholder'] : this.field.placeholder;
            this.isMaskReversed = this.field.params['inputMaskReversed'] ? this.field.params['inputMaskReversed'] : false;
        }
    }

}

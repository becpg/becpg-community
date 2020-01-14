import { Component, ViewEncapsulation, OnInit } from '@angular/core';
import { FormService } from '@alfresco/adf-core';
import { baseHost , WidgetComponent } from '@alfresco/adf-core';
import { DecimalNumberPipe } from '@alfresco/adf-core';

@Component({
  selector: 'app-number-unit',
  templateUrl: './number-unit.component.html',
  styleUrls: ['./number-unit.component.scss'],
  host: baseHost,
  encapsulation: ViewEncapsulation.None
})
export class NumberUnitComponent extends WidgetComponent implements OnInit {

  displayValue: number;

  constructor(public formService: FormService,
              private decimalNumberPipe: DecimalNumberPipe) {
       super(formService);
  }

  ngOnInit() {
      if (this.field.readOnly) {
          this.displayValue = this.decimalNumberPipe.transform(this.field.value);
      } else {
          this.displayValue = this.field.value;
      }
  }

}
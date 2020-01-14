import { Component, OnInit } from '@angular/core';
import { baseHost , WidgetComponent, FormService } from '@alfresco/adf-core';

@Component({
  selector: 'app-number-range',
  templateUrl: './number-range.component.html',
  styleUrls: ['./number-range.component.scss']
})
export class NumberRangeComponent extends WidgetComponent implements OnInit {

  constructor(public formService: FormService) {
    super(formService);
}


  ngOnInit() {
  }

}

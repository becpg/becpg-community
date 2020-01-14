import { Component, OnInit } from '@angular/core';
import { baseHost , WidgetComponent, FormService } from '@alfresco/adf-core';

@Component({
  selector: 'app-date-range',
  templateUrl: './date-range.component.html',
  styleUrls: ['./date-range.component.scss']
})
export class DateRangeComponent extends WidgetComponent implements OnInit {

  constructor(public formService: FormService) {
    super(formService);
}


  ngOnInit() {
  }

}

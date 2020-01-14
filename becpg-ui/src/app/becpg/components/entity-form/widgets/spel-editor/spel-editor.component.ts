import { Component, OnInit } from '@angular/core';
import { baseHost , WidgetComponent, FormService } from '@alfresco/adf-core';

@Component({
  selector: 'app-spel-editor',
  templateUrl: './spel-editor.component.html',
  styleUrls: ['./spel-editor.component.scss']
})
export class SpelEditorComponent extends WidgetComponent implements OnInit {

  constructor(public formService: FormService) {
    super(formService);
}


  ngOnInit() {
  }

}

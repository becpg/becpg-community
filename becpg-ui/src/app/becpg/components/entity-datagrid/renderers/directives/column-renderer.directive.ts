import { Directive, ViewContainerRef } from '@angular/core';

@Directive({
  selector: '[column-renderer]',
})
export class ColumnRendererDirective {
  constructor(public viewContainerRef: ViewContainerRef) { }
}
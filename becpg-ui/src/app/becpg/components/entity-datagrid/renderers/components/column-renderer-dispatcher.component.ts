import {
    Component,
    ComponentFactoryResolver,
    Input,
    OnChanges,
    SimpleChange,
    SimpleChanges,
    ViewChild
} from '@angular/core';
import { EntityListItem } from '../../../../model/EntityListItem';
import { EntityListColumn } from '../../../../model/EntityListColumn';
import { ColumnRendererDirective } from '../directives/column-renderer.directive';
import { ColumnRendererService } from '../services/column-renderer.service';


@Component({
    selector: 'column-renderer-dispatcher',
    template: '<ng-template column-renderer></ng-template>'
})
export class ColumnRendererDispatcherComponent implements OnChanges {
    @Input()
    item: EntityListItem;

    @Input()
    column: EntityListColumn;


    @ViewChild(ColumnRendererDirective)
    private content: ColumnRendererDirective;

    private loaded: boolean = false;
    private componentReference: any = null;

    public ngOnInit;
    public ngDoCheck;

    constructor(private columnRendererService: ColumnRendererService,
                private resolver: ComponentFactoryResolver) {
        const dynamicLifeCycleMethods = [
            'ngOnInit',
            'ngDoCheck',
            'ngAfterContentInit',
            'ngAfterContentChecked',
            'ngAfterViewInit',
            'ngAfterViewChecked',
            'ngOnDestroy'
        ];

        dynamicLifeCycleMethods.forEach((method) => {
            this[method] = this.proxy.bind(this, method);
        });
    }

    ngOnChanges(changes: SimpleChanges) {
        if (!this.loaded) {
            this.loadComponent();
            this.loaded = true;
        }

        Object.keys(changes)
            .map((changeName) => [changeName, changes[changeName]])
            .forEach(([inputParamName, simpleChange]: [string, SimpleChange]) => {
                this.componentReference.instance[inputParamName] = simpleChange.currentValue;
            });

        this.proxy('ngOnChanges', changes);
    }

    private loadComponent() {
        const factoryClass = this.columnRendererService.resolveComponentType(this.column);

        const factory = this.resolver.resolveComponentFactory(factoryClass);
        this.componentReference = this.content.viewContainerRef.createComponent(factory);

        this.componentReference.instance.item = this.item;
        this.componentReference.instance.column = this.column;
    }

    private proxy(methodName, ...args) {
        if (this.componentReference.instance[methodName]) {
            this.componentReference.instance[methodName].apply(this.componentReference.instance, args);
        }
    }
}
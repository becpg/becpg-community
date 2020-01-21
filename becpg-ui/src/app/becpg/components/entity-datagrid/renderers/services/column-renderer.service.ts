import { Injectable, Type } from '@angular/core';
import { EntityListColumn } from '../../../../model/EntityListColumn';
import { DefaultDatagridColumnComponent } from '../components/default-datagrid-column/default-datagrid-column.component';

export interface DynamicComponentModel { type: string; }
export type DynamicComponentResolveFunction = (model: DynamicComponentModel) => Type<{}>;
export class DynamicComponentResolver {
    static fromType(type: Type<{}>): DynamicComponentResolveFunction {
        return () => type;
    }
}

@Injectable({
    providedIn: 'root'
})
export class ColumnRendererService  {


    protected types: { [key: string]: DynamicComponentResolveFunction } = {
        'text': DynamicComponentResolver.fromType(DefaultDatagridColumnComponent)
    };


    protected defaultValue: Type<{}> = DefaultDatagridColumnComponent;
    /**
     * Gets the currently active DynamicComponentResolveFunction for a field type.
     * @param type The type whose resolver you want
     * @param defaultValue Default type returned for types that are not yet mapped
     * @returns Resolver function
     */
    getComponentTypeResolver(type: string, defaultValue: Type<{}> = this.defaultValue): DynamicComponentResolveFunction {
        if (type) {
            return this.types[type] || DynamicComponentResolver.fromType(defaultValue);
        }
        return DynamicComponentResolver.fromType(defaultValue);
    }

  

    /**
     * Finds the component type that is needed to render a form field.
     * @param model Form field model for the field to render
     * @param defaultValue Default type returned for field types that are not yet mapped.
     * @returns Component type
     */
    resolveComponentType(column: EntityListColumn, defaultValue: Type<{}> = this.defaultValue): Type<{}> {
        if (column) {
            const resolver = this.getComponentTypeResolver(column.type, defaultValue);
            return resolver(column);
        }
        return defaultValue;
    }
}
import { EntityVersionHistoryPopupComponent } from './becpg/components/entity-left-side-panel/entity-info-version-manager/entity-version-history/entity-version-history-popup.component';
import { EntityInfoVersionManagerComponent } from './becpg/components/entity-left-side-panel/entity-info-version-manager/entity-info-version-manager.component';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CoreModule, TRANSLATION_PROVIDER, TranslateLoaderService, DialogModule } from '@alfresco/adf-core';
import { ContentModule, VersionManagerModule } from '@alfresco/adf-content-services';
import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { ProcessModule } from '@alfresco/adf-process-services';


import { appRoutes } from './app.routes';
import { PreviewService } from './services/preview.service';
import { FileViewComponent } from './file-view/file-view.component';

// App components
import { AppComponent } from './app.component';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './login/login.component';
import { DocumentlistComponent } from './documentlist/documentlist.component';
import { AppLayoutComponent } from './app-layout/app-layout.component';

//beCPG

import { EntityComponent } from './becpg/components/entity/entity.component';
import { EntityHeaderComponent } from './becpg/components/entity-header/entity-header.component';
import { EntityViewsComponent } from './becpg/components/entity-views/entity-views.component';
import { EntityViewToolbarComponent } from './becpg/components/entity-view-toolbar/entity-view-toolbar.component';

//Form
import { EntityFormComponent } from './becpg/components/entity-form/entity-form.component';
import { AutocompleteComponent } from './becpg/components/entity-form/widgets/autocomplete/autocomplete.component';

//Datagrid
import { EntityDatagridComponent } from './becpg/components/entity-datagrid/entity-datagrid.component';
import { ColumnRendererDispatcherComponent } from './becpg/components/entity-datagrid/renderers/components/column-renderer-dispatcher.component';
import { DefaultDatagridColumnComponent } from './becpg/components/entity-datagrid/renderers/components/default-datagrid-column/default-datagrid-column.component';
import { ColumnRendererDirective } from './becpg/components/entity-datagrid/renderers/directives/column-renderer.directive';


import { EntityLogoComponent } from './becpg/components/entity-logo/entity-logo.component';
import { FlexLayoutModule } from '@angular/flex-layout';


import { TableModule } from 'primeng/table';
import {MultiSelectModule} from 'primeng/multiselect';
import {ContextMenuModule} from 'primeng/contextmenu';
import {ButtonModule} from 'primeng/button';
import {MenuModule} from 'primeng/menu';
import {SplitButtonModule} from 'primeng/splitbutton';
import {DropdownModule} from 'primeng/dropdown';


import { MatSidenavModule} from '@angular/material/sidenav';
import { HttpClientModule } from '@angular/common/http';
import { MatDialogModule, MatSelectModule, MatCardModule, MatGridListModule, MatButtonModule, MatIconModule } from '@angular/material';

import { registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import localeDe from '@angular/common/locales/de';
import localeIt from '@angular/common/locales/it';
import localeEs from '@angular/common/locales/es';
import localeJa from '@angular/common/locales/ja';
import localeNl from '@angular/common/locales/nl';
import localePt from '@angular/common/locales/pt';
import localeNb from '@angular/common/locales/nb';
import localeRu from '@angular/common/locales/ru';
import localeCh from '@angular/common/locales/zh';
import localeAr from '@angular/common/locales/ar';
import localeCs from '@angular/common/locales/cs';
import localePl from '@angular/common/locales/pl';
import localeFi from '@angular/common/locales/fi';
import localeDa from '@angular/common/locales/da';
import localeSv from '@angular/common/locales/sv';
import { MtlangueComponent } from './becpg/components/entity-form/widgets/mtlangue/mtlangue.component';
import { ColorComponent } from './becpg/components/entity-form/widgets/color/color.component';
import { NumberUnitComponent } from './becpg/components/entity-form/widgets/number-unit/number-unit.component';
import { NumberRangeComponent } from './becpg/components/entity-form/widgets/number-range/number-range.component';
import { DateRangeComponent } from './becpg/components/entity-form/widgets/date-range/date-range.component';
import { SpelEditorComponent } from './becpg/components/entity-form/widgets/spel-editor/spel-editor.component';
import { NutriscoreComponent } from './becpg/components/entity-form/widgets/nutriscore/nutriscore.component';
import { EntityLogoUploadPopupComponent } from './becpg/components/entity-logo/entity-logo-upload-popup/entity-logo-upload-popup.component';
import { NodeRefToIdPipe } from './becpg/pipes/node-ref-to-id.pipe';



registerLocaleData(localeFr);
registerLocaleData(localeDe);
registerLocaleData(localeIt);
registerLocaleData(localeEs);
registerLocaleData(localeJa);
registerLocaleData(localeNl);
registerLocaleData(localePt);
registerLocaleData(localeNb);
registerLocaleData(localeRu);
registerLocaleData(localeCh);
registerLocaleData(localeAr);
registerLocaleData(localeCs);
registerLocaleData(localePl);
registerLocaleData(localeFi);
registerLocaleData(localeDa);
registerLocaleData(localeSv);



@NgModule({
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        RouterModule.forRoot(
            appRoutes // ,
            // { enableTracing: true } // <-- debugging purposes only
        ),

        // ADF modules
        CoreModule.forRoot(),
        ContentModule.forRoot(),
        ProcessModule.forRoot(),
        TranslateModule.forRoot({
            loader: { provide: TranslateLoader, useClass: TranslateLoaderService }
        }),
        //PrimeNG
        TableModule,
        MultiSelectModule,
        ContextMenuModule,
        ButtonModule,
        MenuModule,
        SplitButtonModule,
        DropdownModule,

        MatSidenavModule,
        HttpClientModule,
        FlexLayoutModule,
        MatDialogModule,
        MatSelectModule,
        MatCardModule,
        MatGridListModule,
        MatButtonModule,
        MatIconModule,
        VersionManagerModule,
        DialogModule
    ],
    declarations: [
        AppComponent,
        HomeComponent,
        LoginComponent,
        DocumentlistComponent,
        AppLayoutComponent,
        FileViewComponent,
        EntityComponent,
        EntityHeaderComponent,
        EntityViewsComponent,
        EntityViewToolbarComponent,
        EntityFormComponent,
        EntityDatagridComponent,
        EntityLogoComponent,
        AutocompleteComponent,
        MtlangueComponent,
        ColorComponent,
        NumberUnitComponent,
        NumberRangeComponent,
        DateRangeComponent,
        SpelEditorComponent,
        NutriscoreComponent,
        EntityLogoUploadPopupComponent,
        EntityInfoVersionManagerComponent,
        EntityVersionHistoryPopupComponent,
        NodeRefToIdPipe,
        ColumnRendererDispatcherComponent,
        DefaultDatagridColumnComponent,
        ColumnRendererDirective
    ],
    entryComponents: [
        AutocompleteComponent,
        MtlangueComponent,
        ColorComponent,
        NumberUnitComponent,
        NumberRangeComponent,
        DateRangeComponent,
        SpelEditorComponent,
        NutriscoreComponent,
        EntityLogoUploadPopupComponent,
        EntityVersionHistoryPopupComponent,
        DefaultDatagridColumnComponent
    ],

    providers: [
        PreviewService,
        {
            provide: TRANSLATION_PROVIDER,
            multi: true,
            useValue: {
                name: 'app',
                source: 'resources'
            }
        }
    ],

    bootstrap: [AppComponent]
})
export class AppModule {
}

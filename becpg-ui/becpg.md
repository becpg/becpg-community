
Dev will be split in sprint:
 - 2 weeks sprint for each components of complexity 1, complexity of 2 will require  2 sprints ...  
 - we will do behavior-driven development (BDD) as much as possible [Write tests before code]
 - dev will be in a special app (becpg-ui) created with adf on the future beCPG branch
 - it will move at the end to alfresco-content-app as a module


Complexity total env 40 = 40 * 5J = 200J/ Homme
80 semaines / 3 (Matthieu/ Rabah / Evelyne) -->  26 Semaines [env 7 mois)]

# Workloads:

Projet setup and architecture [Complexity 1]

# Components

# Header

## Entity
ng generate component becpg/components/entity-header [Complexity 1]
ng generate component becpg/components/entity-score  [Complexity 1]

## Common
ng generate component becpg/components/current-users [Complexity 1]
ng generate component becpg/components/search [Complexity 1]

# Entity page

Define entity main layout page left-panel, main-panel, side-panel

ng generate component becpg/components/entity [Complexity 1]

## left-panel

ng generate component becpg/components/entity-logo [Complexity 1]
ng generate component becpg/components/entity-views // List of views [Complexity 1]

## main-panel

ng generate component becpg/components/entity-view-toolbar [Complexity 1]

ng generate component becpg/components/entity-form [Complexity 3]

Features
- spel editor
- multilingual
- autocomplete
- convert share form tool 
- ...

ng generate component becpg/components/entity-datagrid [Complexity 3]

Features
- renderers
- editors
- sort 
- ...

ng generate component becpg/components/entity-custom-datagrid
ng generate component becpg/components/entity-dyncharact-datagrid ?


ng generate component becpg/components/entity-documents [Complexity 1]
ng generate component becpg/components/entity-reports [Complexity 1]
ng generate component becpg/components/entity-gantt-view [Complexity 2]

## side-panel

### Informations tab

ng generate component becpg/components/entity-logo [Complexity 1]
ng generate component becpg/components/entity-summary [Complexity 1]
ng generate component becpg/components/entity-versions [Complexity 1]

ng generate component becpg/components/entity-projects [Complexity 1]
ng generate component becpg/components/entity-processes [Complexity 1]

### Comments tab
ng generate component becpg/components/entity-comments [Complexity 1]

### Progression tabs
ng generate component becpg/components/entity-catalogs [Complexity 1]

### Activity tabs
ng generate component becpg/components/entity-activities [Complexity 1]

# Bulk-edit page
ng generate component becpg/components/bulk-edit [Complexity 1]

## main-panel

ng generate component becpg/components/bulkedit-toolbar [Complexity 1]
ng generate component becpg/components/bulkedit-props-datagrid -> use entity-datagrid [Complexity 1]
ng generate component becpg/components/bulkedit-lists-datagrid -> use entity-datagrid [Complexity 1]

## side-panel

ng generate component becpg/components/bulkedit-profiles  [Complexity 1]

### Properties tab
ng generate component becpg/components/bulkedit-props-picker [Complexity 1]

### Lists tab
ng generate component becpg/components/bulkedit-lists-picker [Complexity 1]


# Try bar
Allow to switch from entities, bulk-edit and compare [Complexity 1]

ng generate component becpg/components/try-toolbar [Complexity 1]

# Dialog (A reflèchir)

ng generate component becpg/components/charact-details [Complexity 1]
ng generate component becpg/components/labeling-details [Complexity 1]
ng generate component becpg/components/nut-databases [Complexity 1]
ng generate component becpg/components/rapid-link [Complexity 1]

# Services

A définir

# Model

A définir


# Datagrid column definition example:

{
    "type": "model",
    "id": "plm",
    "datagrids": {
        "bcpg:compoList": {
            "default": {
                "bcpg:variantIds": {
                    "force": true,
                    "hidden": true,
                    "readOnly": true,
                    "renderer": "variant"
                },
                "bcpg:depthLevel": {
                    label: "Overrided label"
                },
                ...


# EntityView definition example:


{
"type" : "view",
"id" : "plm"
"views" : {
   "default": {},
   "properties":{
      cards: [
         {component: "entity-form", options: {formId: "default"}}
         ]

   },
   "compoList": {
     cards: [
         {component: "entity-datagrid", options: {},  cols: 1, rows: 1, colspan: 2},
         {component: "entity-datagrid", options: {itemType: "bcpg:dynamicCharactList" },  cols: 1, rows: 2},
         {component: "entity-custom-datagrid", options: {},  cols: 2, rows: 2}
     ]
   }
...

# Form definition example:
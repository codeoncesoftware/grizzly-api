import { Component, OnInit, Inject, ViewChild, ElementRef } from '@angular/core';
import { Resource } from 'src/app/shared/models/Resource';
import { ParameterType } from 'src/app/shared/resource-types';
import { MatChipInputEvent } from '@angular/material/chips';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialog } from '@angular/material/dialog';
import { MatStepper } from '@angular/material/stepper';
import { Parameter } from 'src/app/shared/models/Parameter';
import { COMMA, ENTER } from '@angular/cdk/keycodes';

// NG2 UPLOADER IMPORTS
import { FileUploader } from 'ng2-file-upload';
import { Store } from '@ngrx/store';
import * as containerActions from '../../store/container/container.actions';
import * as dbsourceActions from '../../store/dbsource/dbsource.actions';
import { ContainerState } from 'src/app/store/container/container.state';
import { ResourceGroup } from 'src/app/shared/models/ResourceGroup';

import { Container } from 'src/app/shared/models/Container';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { FileExplorerComponent } from 'src/app/container/file-explorer/file-explorer.component';
import { APIResponse } from 'src/app/shared/models/APIResponse';
import { DBSourcesState } from 'src/app/store/dbsource/dbsource.state';
import { DBSource } from 'src/app/shared/models/DBSource';
import { Database } from 'src/app/shared/models/Database';

import { JsonEditorComponent, JsonEditorOptions } from 'ang-jsoneditor';
import { ProjectsState } from 'src/app/store/project/project.state';
import { Project } from 'src/app/shared/models/Project';
import { SelectFilesModalService } from './select-files-modal/select-files-modal.service';
import { ResourceFile } from 'src/app/shared/models/ResourceFile';
import { CustomQuery } from 'src/app/shared/models/CustomQuery';
import { FileNode } from 'src/app/shared/models/FileNode';

import { Snippets } from './query-snippets';
import { MessageService } from 'src/app/shared/message-modal/message.service';

/**
 * IMPORTANT !!
 *
 * This Component needs to be Refactored into multiple Components
 * For Each Operation, one single Component
 * XSL, Thymeleaf, FreeMarker, Query, File and The Parent Component
 */

@Component({
  selector: 'app-resource-modal',
  templateUrl: './resource-modal.component.html',
  styleUrls: ['./resource-modal.component.scss']
})

export class ResourceModalComponent implements OnInit {

  // JSON EDITOR
  public editorOptions: JsonEditorOptions;
  @ViewChild(JsonEditorComponent, { static: false }) editor: JsonEditorComponent;
  /** File Explorer Component */
  @ViewChild('prem', { static: false }) fileExplorer: FileExplorerComponent;
  @ViewChild('sec', { static: false }) sec: FileExplorerComponent;
  /** Stepper Component */
  @ViewChild('stepper', { static: false }) stepper: MatStepper;
  /** New Collection Name Input */
  @ViewChild('newCollection', { static: false }) newCollectionInput: ElementRef;
  /** Wizard Step by Step Variables */
  noResources = false; // To prevent skiping a step
  secondFormGroup: FormGroup;
  /** Select File ID */
  selectedFileId = '';
  selectedFileName = '';
  selected = false;
  bodyExists: boolean;
  selectedParamType = 'String';

  selectedFileSecId = '';
  selectedFileSecName = '';
  selectedSec = false;
  /** Switch Edit Mode */
  editMode = false;
  /** disable Default Value Input On File Type Select */
  disableDefaultValue = false;

  types = ParameterType;
  param = new Parameter();
  response = new APIResponse();
  responses = [] as APIResponse[];
  consumes = [] as string[];
  produces = [] as string[];
  parameters = [] as Parameter[];
  requestBody = new Parameter();
  bodyContent: string;
  resource: Resource;
  notUnique = false;
  progress = 0;
  containerToSave: Container = new Container();
  activecontainer: Container;
  primaryFiles: string[] = [];
  secondaryFiles: any[] = [];
  showModal = true;
  apiUnicity = true;
  emptyPath = false;
  /** NG2 UPLOADER */
  public uploader: FileUploader = new FileUploader({});
  public hasBaseDropZoneOver = false;

  // Chips Variables
  visible = true;
  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeysCodes: number[] = [ENTER, COMMA];

  /** Dbsources Section */
  dbsourcesList: DBSource[] = [];
  databasesList: Database[] = [];
  collectionsList: string[] = [];
  query: any;
  updateQuery: any;

  /** Add New Collection */
  newCollectionName: string;
  oldcollectionName: string;
  addCollectionBool = false;

  /** Active Project */
  project = new Project();

  /** This variable is for XML Content Upload */
  xmlFile: any;

  /** Name For File To Upload In Case Of FIle Handling */
  fileNameForUpload: string;

  /** Array to Share Data Between search-file Component and upload-fie-component */
  searchSelectedFiles: FileNode[] = [];
  showTree = false;
  showSelectBtn = false;

  Rgroup: ResourceGroup;
  // Snippets Names
  querySnippetNames: string[];

  constructor(private containersStore: Store<ContainerState>,
              private dbsourcesStore: Store<DBSourcesState>,
              private projectStore: Store<ProjectsState>,
              private formBuilder: FormBuilder,
              public dialog: MatDialog,
              public resourceDialogRef: MatDialogRef<ResourceModalComponent>,
              public selectFilesModalService: SelectFilesModalService,
              public messageBoxService: MessageService,
              @Inject(MAT_DIALOG_DATA) public data: any) {

    this.editorOptions = new JsonEditorOptions();
    this.editorOptions.mainMenuBar = false;
    this.editorOptions.mode = 'code';
    this.editorOptions.modes = ['code']; // set all allowed modes
    // Snippets Names
    this.querySnippetNames = Array.from(Snippets.keys());
  }

  ngOnInit() {
    /** Declaration of Listeners for The State Change Starts */
    this.containersStore.select('containers').subscribe(res => {
      // tslint:disable-next-line: no-string-literal
      if (!res['active'].hierarchy) {
        this.noResources = true;
      }
      // tslint:disable-next-line: no-string-literal
      this.activecontainer = res['active'];

      this.Rgroup = this.data.Rgroup;
      // Create a default ResourceGroup if no RG exists or place the new API in the last RG
      if (!this.Rgroup) {
        if (this.activecontainer.resourceGroups.length === 1 && this.activecontainer.resourceGroups[0].name === 'Authentication Grizzly') {
          this.Rgroup = new ResourceGroup('Untitled');
          this.activecontainer.resourceGroups.push(this.Rgroup);
        } else {
          this.Rgroup = this.activecontainer.resourceGroups[this.activecontainer.resourceGroups.length - 1];
        }
      }
    });

    this.dbsourcesStore.select('dbsources').subscribe(res => {
      // tslint:disable-next-line: no-string-literal
      this.dbsourcesList = res['dbsources'];
      if (this.resource && this.resource.customQuery && this.resource.customQuery.database) {
        // Seletec Databases List for Each Data Source
        this.getDatabases(this.resource.customQuery.datasource);
      }

    });
    this.projectStore.select('projects').subscribe(resState => {
      // tslint:disable-next-line: no-string-literal
      this.project = resState['active'];
    });
    /** Declaration of Listeners for The State Change Ends */
    // INITIATE Resource Object
    this.resource = { ...this.data.resource };

    if (this.resource.customQuery) {
      this.resource.customQuery.datasource = this.project.dbsourceId;
      this.resource.customQuery.database = this.project.databaseName;
    } else {
      this.resource.customQuery = new CustomQuery();
      this.resource.customQuery.datasource = this.project.dbsourceId;
      this.resource.customQuery.database = this.project.databaseName;
    }

    /** Get Collections List */
    this.selectCollections();

    if (!this.resource.executionType) {
      this.resource.executionType = 'Query';
    }

    if (this.resource.executionType === 'File' && this.resource.parameters[0]) {
      this.fileNameForUpload = this.resource.parameters[0].name;
    }

    this.query = JSON.parse(this.resource.customQuery.query);
    if (this.data.resource.resourceFile && this.data.resource.resourceFile.fileUri) {
      this.editMode = true;
      this.selectedFileId = this.resource.resourceFile.fileId;
      this.selectedFileName = this.resource.resourceFile.fileUri;
      this.secondaryFiles = this.resource.secondaryFilePaths;
    }
    if (this.resource.parameters) {
      this.parameters = this.resource.parameters;
    } else {
      this.parameters = [];
    }
    if (this.resource.customQuery) {
      // If a Data Source is already Selected, Load Databases List
      if (this.resource.customQuery.datasource) {
        this.getDatabases(this.resource.customQuery.datasource);
      }
      // If a Database is already selected, Load Collections List
      if (this.resource.customQuery.database) {
        this.getCollections(this.resource.customQuery.database);
      }
    }

    /** Reactive Form Fields */
    this.secondFormGroup = this.formBuilder.group({
      resourceName: ['', ],
      pathName: ['', Validators.required],
      httpMethod: ['', Validators.required],
      resourceDesription: ['', ],
      paramValue: ['', ],
      resourceSummary: ['', ],
      executionType: ['', ],
      datasource: ['', ],
      collection: ['', ],
      newCollection: ['', ],
      database: ['', ],
      queryType: ['', ],
      fileNameForUpload: ['', ],
      newCollectionName: ['', ],
      many: ['', ]
    });
    this.parameters.forEach(element => {
      if (element.in === 'Body' && element.value) {
        this.bodyExists = true;
      }
    });

    this.selectFilesModalService.selectedFile.subscribe(res => {
      if (res) {
        let resourceFile;
        // Initiate ResourceFile To Save
        if (res.file.name.lastIndexOf(this.activecontainer.id) > -1) {
          resourceFile = new ResourceFile(res.file.fileId, res.file.name.substr(res.file.name.lastIndexOf(this.activecontainer.id) + this.activecontainer.id.length + 1, res.length).split('\\').join('/'));
        } else {
          resourceFile = new ResourceFile(res.file.fileId, res.file.name);
        }
        if (res.mode === 'primary') {
          this.resource.resourceFile = resourceFile;
        } else {
          if (this.resource.secondaryFilePaths.findIndex(f => f.fileId === resourceFile.fileId) < 0) {
            this.resource.secondaryFilePaths.push(resourceFile);
          }
        }
      }
    });



  }

  /** ADD Param to the list of parameters */
  public addNewParam() {
    if (this.param.name && this.param.type && this.param.in && this.getParamIndex(this.param) < 0) {
      if (this.param.type.toLowerCase() === 'file') {
        if (!this.resource.consumes.find(str => str === 'multipart/form-data')) {
          this.resource.consumes.push('multipart/form-data');
        }
      }
      this.parameters.push(this.param);
      this.param = new Parameter();
    }
  }

  public checkIfFileIsSelected() {
    if (this.param.type.toLowerCase() === 'file') {
      this.disableDefaultValue = true;
    } else {
      this.disableDefaultValue = false;

    }
  }

  /**  DELETE Param on Click Before Upload */
  public deleteParam(i: number) {
    if (this.parameters[i].in === 'Body') {
      this.bodyExists = false;
    }

    this.parameters.splice(i, 1);
  }

  /** ADD New Reponse Code & Description */
  public addNewResponse() {
    if (this.response.code && this.response.description) {
      this.resource.responses.push(this.response);
      this.response = new APIResponse();
    }
  }
  /** DELETE Response on Click */
  public deleteResponse(i: number) {
    this.resource.responses.splice(i, 1);
  }

  /** DELETE Selected File From Uploader Queue Before Upload */
  public removeFile(fileName: string, fileSize: number) {
    this.uploader.queue = this.uploader.queue
      .filter(item =>
        ((item._file.name !== fileName) && (item._file.size !== fileSize)));
  }

  addToConsumes(consumeType) {
    this.resource.consumes[0] = consumeType;
  }

  // SECTION for Chips for Consume Types START
  public addToProduces(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;
    // Add our fruit
    if ((value || '').trim()) {
      this.resource.produces = [];
      this.resource.produces.push(value);
    }
    // Reset the input value
    if (input) {
      input.value = '';
    }
  }
  public removeFromProduces(): void {
    this.resource.produces = [];
  }

  public getParamIndex(param: Parameter): number {
    return this.parameters.findIndex((x: Parameter) => x.name === param.name);
  }

  public getGroupIndex(group: ResourceGroup): number {
    return this.data.container.resourceGroups.findIndex((x: ResourceGroup) => x.name === group.name);
  }

  /** NG UPLOADER METHOD */
  public fileOverBase(ev: boolean): void {
    this.hasBaseDropZoneOver = ev;
  }

  /** Get Selected File ID */
  public onSelectedFile(res: string): void {
    if (res) {
      this.selected = true;
      this.selectedFileId = res.substr(0, res.lastIndexOf('#'));
      this.selectedFileName = res.substr(res.lastIndexOf(this.activecontainer.id) + this.activecontainer.id.length + 1, res.length);
      this.selectedFileName = this.selectedFileName.split('\\').join('/');
    } else {
      this.selected = false;
      this.selectedFileId = '';
      this.selectedFileName = '';
    }
  }

  public onSelectedSecondFiles(res): void {
    if (res.length !== 0) {
      this.selectedSec = true;
      this.secondaryFiles = [];
      res.forEach(element => {
        this.selectedFileSecId = element.substr(0, element.lastIndexOf('#'));
        this.selectedFileSecName = element.substr(element.lastIndexOf(this.activecontainer.id) + this.activecontainer.id.length + 1, element.length);
        this.selectedFileSecName = this.selectedFileSecName.split('\\').join('/');
        this.secondaryFiles.push({ fileId: this.selectedFileSecId, fileUri: this.selectedFileSecName });
      });
    } else {
      this.selectedSec = false;
      this.secondaryFiles = [];
    }
  }

  /** On Reset Click delete parameters list and selected resource */
  public reset(): void {
    this.parameters = [];
    this.selected = false;
    this.selectedFileId = '';
    this.selectedFileName = '';
    this.fileExplorer.uncheckAll();
  }

  /** Save Resource In GridFs Using NgRx */
  public saveResource(): void {

    // Create Default Group if No Resource Group exists

    if (this.resource.executionType === 'File') {
      if (this.fileNameForUpload) {
        this.parameters = [];
        this.param.in = 'formData';
        this.param.name = this.fileNameForUpload;
        this.param.type = 'File';
        this.addNewParam();
        this.resource.parameters = this.parameters;
        this.fileNameForUpload = this.resource.parameters[0].name;
      } else {
        this.param.in = 'formData';
        this.param.name = '_id';
        this.param.type = 'String';
      }
    }
    if (this.resource.executionType !== 'Query' && this.resource.executionType !== 'File') {
      this.resource.customQuery.query = null;
      this.resource.customQuery.type = null;
      this.resource.pageable = null;
    }

    this.resource.parameters = this.parameters;
    this.resource.path = this.resource.path.toLocaleLowerCase();
    // Verifying that the endpoint starts with /
    this.prepareEndpoint();
    this.containerToSave = JSON.parse(JSON.stringify(this.data.container));
    const groupIndex = this.getGroupIndex(this.Rgroup);
    const groupResources = this.containerToSave.resourceGroups[groupIndex].resources;
    const resourceIndex = this.getResourceIndex(this.resource, groupResources);
    // ADD API
    if (this.data.editMode === false) {
      if (resourceIndex >= 0) {
        this.apiUnicity = false;
      } else {
        this.apiUnicity = true;
        groupResources.push(this.resource);
        this.containerToSave.resourceGroups[this.getGroupIndex(this.Rgroup)].resources = groupResources;
        this.containersStore.dispatch(new containerActions.UpdateContainer(this.containerToSave, 'Resource Added'));
        this.resourceDialogRef.close();
      }

      // EDIT API
    } else {
      if (this.data.resource.httpMethod === this.resource.httpMethod && this.data.resource.path === this.resource.path) {
        // nothing changed in terms of path and method
        groupResources[resourceIndex] = this.resource;
        this.containerToSave.resourceGroups[this.getGroupIndex(this.Rgroup)].resources = groupResources;
        this.containersStore.dispatch(new containerActions.UpdateContainer(this.containerToSave, 'Resource Updated'));
        this.resourceDialogRef.close();
      } else {
        if (resourceIndex >= 0) {
          this.apiUnicity = false;
        } else {
          const oldIndex = this.getResourceIndex(this.data.resource, groupResources);
          groupResources[oldIndex] = this.resource;
          this.containerToSave.resourceGroups[this.getGroupIndex(this.Rgroup)].resources = groupResources;
          this.apiUnicity = true;
          this.containersStore.dispatch(new containerActions.UpdateContainer(this.containerToSave, 'Resource Updated'));
          this.resourceDialogRef.close();
        }
      }
    }
  }

  private prepareEndpoint() {
    this.resource.path = this.resource.path.toLocaleLowerCase();
    if (this.resource.path.charAt(0) !== '/') {
      this.resource.path = '/' + this.resource.path;
    }
  }

  public getResourceIndex(resourceSearchIndex: Resource, group): number {
    return group.findIndex((x: Resource) => x.path.toUpperCase() === resourceSearchIndex.path.toUpperCase() && x.httpMethod === resourceSearchIndex.httpMethod);
  }

  /** MatStepper Actions Control */
  public goBack(stepper: MatStepper): void {
    stepper.previous();
  }

  onInput() {
    this.checkEmptyPath();
    this.apiUnicity = true;
  }

  getPathParams(path: string) {
    const params = [];
    while (path.length > 0) {
      const param = path.substr(path.indexOf('{') + 1, path.indexOf('}') - path.indexOf('{') - 1);
      if (param.length > 0) {
        params.push(param);
      }
      path = path.slice(path.indexOf(param) + param.length + 1, path.length);
    }
    if (params.length === 0) {
      this.parameters.forEach(element => {
        const index = this.parameters.findIndex(p => p.in === 'Path');
        this.parameters.splice(index, 1);
      });
    } else {
      params.forEach(element => {
        if (this.parameters.findIndex(p => p.in + p.name === 'Path' + element) < 0) {
          this.parameters.push(new Parameter('Path', element, 'String'));
        }
      });
    }

  }


  public goForward(stepper: MatStepper): void {
    if (stepper.selectedIndex === 0) {
      this.checkEmptyPath();
      if (this.verifyUnicity() && !this.emptyPath) {
        stepper.next();
      }
    } else {
      stepper.next();
    }
  }

  private checkEmptyPath() {
    if (this.resource.path === '/' || !this.resource.path) {
      this.emptyPath = true;
    } else {
      this.emptyPath = false;
    }
  }

  public selectCollections() {
    this.getDatabases(this.project.dbsourceId);
  }

  /**
   * Fetch the DataBase from the selected Data Source
   * @param value the requested DataBase
   */
  private getDatabases(value: string): void {
    this.dbsourcesList.forEach(dbSource => {
      if (dbSource && dbSource.id === value) {
        this.databasesList = dbSource.databases;
      }
    });
    if (this.resource.customQuery && this.resource.customQuery.database) {
      this.getCollections(this.resource.customQuery.database);
    }

  }

  /**
   * Load the selected Database Collection List
   * @param value The Collections List
   */
  private getCollections(value): void {
    this.databasesList.forEach(database => {
      if (database.name === value) {
        this.collectionsList = database.collections.filter(coll => coll !== 'authentication_user');
      }
    });
    if (this.resource.customQuery && this.resource.customQuery.collectionName && !this.collectionsList.find(coll => coll === this.resource.customQuery.collectionName)) {
      this.collectionsList.push(this.resource.customQuery.collectionName);
    }
  }

  addCollection() {
    this.addCollectionBool = true;
    this.oldcollectionName = this.resource.customQuery.collectionName;
    this.newCollectionName = this.resource.customQuery.collectionName;
  }
  confirmNewCollection() {
    this.addCollectionBool = false;
    if (!this.collectionsList.find(coll => coll === this.newCollectionName)) {
      // this.collectionsList.push(this.newCollectionName);
      this.resource.customQuery.collectionName = this.newCollectionName;
      this.dbsourcesStore.dispatch(new dbsourceActions.AddNewCollection({ dbsourceId: this.project.dbsourceId, databaseName: this.project.databaseName, containerId: this.activecontainer.id, collectionName: this.resource.customQuery.collectionName }));
    } else {
      this.cancelNewCollection();
    }
  }
  cancelNewCollection() {
    this.addCollectionBool = false;
    this.newCollectionName = '';
    this.resource.customQuery.collectionName = this.oldcollectionName;
  }

  /**
   * Receive JSON Query Change
   * @param query for database
   */
  public getJsonChange(query): void {
    this.resource.customQuery.query = JSON.stringify(query);
  }

  xmlFileChanged(e) {
    this.xmlFile = e.target.files[0];
    const fileReader = new FileReader();
    fileReader.onload = () => {
      this.param.value = fileReader.result.toString();
      this.param.name = 'body';
      this.param.type = 'String';
      this.param.in = 'Body';
      this.addNewParam();
      this.bodyExists = true;
      this.bodyContent = fileReader.result.toString();
    };
  }

  displayBody() {
    if (this.bodyContent && !this.xmlFile) {
      this.param.in = 'Body';
      this.param.name = 'body';
      this.param.type = 'String';
      this.param.value = this.bodyContent;
      this.addNewParam();
      this.bodyExists = true;
    }
  }

  // Projection Fields Action
  updateProjectionFields(fields: string[]) {
    const list: string[] = [];
    fields.forEach(field => list.push(field));

    this.resource.fields = list;
  }

  checkPageable() {
    this.resource.pageable = !this.resource.pageable;
  }

  verifyUnicity() {

    this.containerToSave = JSON.parse(JSON.stringify(this.data.container));
    const groupIndex = this.getGroupIndex(this.Rgroup);
    const groupResources = this.containerToSave.resourceGroups[groupIndex].resources;
    const resourceIndex = this.getResourceIndex(this.resource, groupResources);

    if (resourceIndex >= 0 && resourceIndex !== this.data.ressourceIndex) {
      this.apiUnicity = false;
      return false;
    } else {
      this.apiUnicity = true;
      return true;

    }

  }

  showFilesTree() {
    this.showTree = !this.showTree;
    this.showSelectBtn = true;
  }

  changeSearchValue(searchValue) {
    this.sec.onSearchChange(searchValue);
  }

  openSelectFilesModal(mode) {
    this.selectFilesModalService.openModal({ activecontainerId: this.activecontainer.id, mode });
  }

  unSelectFile(fileToDeselect: FileNode, mode: string) {
    if (mode === 'primary') {
      this.resource.resourceFile = null;
    } else {
      this.resource.secondaryFilePaths = this.resource.secondaryFilePaths.filter(file => file.fileId !== fileToDeselect.fileId);
    }
  }

  addSnippet(snippetName: string) {
    if ((this.editor.getText().length > 0) && (this.editor.getText() !== '{}')) {
      this.messageBoxService.openWarning('Replace Query', 'Your current query will be replaced, continue ?', {
        info: {
          msg: 'Replace ' + this.editor.getText() + ' with ' + Snippets.get(snippetName).query
        }
      }).afterClosed().subscribe(res => {
        if (res) {
          this.confirmSnippet(snippetName);
        }
      });
    } else {
      this.confirmSnippet(snippetName);
    }

  }

  private confirmSnippet(snippetName) {
    // Get the Query
    this.editor.set(JSON.parse(Snippets.get(snippetName).query));
    // Empty the parameters Array for the Ressource
    this.parameters = [];
    // Set the new Query Params
    Snippets.get(snippetName).params.forEach(param => {
      this.param.in = param.in;
      this.param.name = param.name;
      this.param.type = param.type;
      this.param.value = param.value;
      this.addNewParam();
    });
  }

}

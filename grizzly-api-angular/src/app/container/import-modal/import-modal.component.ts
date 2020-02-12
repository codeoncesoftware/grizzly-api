import { Component, OnInit, Inject, Output, ViewChild, Input } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatTreeFlattener, MatTreeFlatDataSource } from '@angular/material/tree';
import { FileUploader } from 'ng2-file-upload';
import { ResourceService } from 'src/app/resource/resource.service';
import { FlatTreeControl } from '@angular/cdk/tree';
import { of } from 'rxjs';
import { Store } from '@ngrx/store';
import { ContainerState } from 'src/app/store/container/container.state';
import * as containerActions from '../../store/container/container.actions';
import { Container } from '../../shared/models/Container';
import { Project } from 'src/app/shared/models/Project';
import { ToastrService } from 'ngx-toastr';
import { DBSourcesState } from 'src/app/store/dbsource/dbsource.state';
import { DBSource } from 'src/app/shared/models/DBSource';
import { ProjectsState } from 'src/app/store/project/project.state';
import { MessageService } from 'src/app/shared/message-modal/message.service';

/** File node data with nested structure. */
export interface FileNode {
  name: string;
  type: string;
  fileId: string;
  children?: FileNode[];
}

/** Flat node with expandable and level information */
export interface TreeNode {
  name: string;
  type: string;
  level: number;
  fileId: string;
  expandable: boolean;
}

@Component({
  selector: 'app-import-modal',
  templateUrl: './import-modal.component.html',
  styleUrls: ['./import-modal.component.scss']
})
export class ImportModalComponent implements OnInit {

  // Toggle Import Sections
  showGitSection = true;
  showZipSection = false;
  hide = false;

  // Git variables
  gitUrl: string;
  gitUrlBeforeCheck: string;
  branchsList: string[] = [];
  loadingBranches = false;
  checksuccess = false;
  gitbranch: string;
  gitRepoType = 'public';
  gitUsername: string;
  gitPassword: string;
  gitError = false;
  unexpectedErrorMsg: string;

  public uploader: FileUploader = new FileUploader({});
  loading = false;

  // Hierarchy
  treeControl: FlatTreeControl<TreeNode>;

  /** The TreeFlattener is used to generate the flat list of items from hierarchical data. */
  treeFlattener: MatTreeFlattener<FileNode, TreeNode>;

  /** The MatTreeFlatDataSource connects the control and flattener to provide data. */
  dataSource: MatTreeFlatDataSource<FileNode, TreeNode>;

  files: FileNode[] = [];
  hierarchy: FileNode;
  container: Container = new Container();
  activeProject: Project;
  hierarchyLoaded = false;

  @Input() showHierarchy = true;
  zipError = false;

  constructor(private toaster: ToastrService,
    private resourceService: ResourceService,
    public dialogRef: MatDialogRef<ImportModalComponent>,
    private projectStore: Store<ProjectsState>,
    private store: Store<ContainerState>,
    private messageBoxService: MessageService,
    @Inject(MAT_DIALOG_DATA) public data: any) {
    // Hierarchy
    this.treeFlattener = new MatTreeFlattener(
      this.transformer,
      this.getLevel,
      this.isExpandable,
      this.getChildren);

    this.treeControl = new FlatTreeControl<TreeNode>(this.getLevel, this.isExpandable);
    this.dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);
    this.dataSource.data = this.files;

    if (this.data.showLoadedHierarchy === true) {
      this.hierarchyLoaded = true;
    }
  }

  ngOnInit() {
    this.store.select('containers').subscribe(state => {
      // tslint:disable-next-line: no-string-literal
      this.container = state['active'];
    });
    this.projectStore.select('projects').subscribe(state => {
      // tslint:disable-next-line: no-string-literal
      this.activeProject = state['active'];
    });
    this.branchsList.push('master');
    this.gitbranch = 'master';
  }

  /** Transform the data to something the tree can read. */
  transformer(node: FileNode, level: number) {
    return {
      name: node.name,
      type: node.type,
      fileId: node.fileId,
      level,
      expandable: !!node.children
    };
  }

  /** Get the level of the node */
  getLevel(node: TreeNode) {
    return node.level;
  }

  /** Return whether the node is expanded or not. */
  isExpandable(node: TreeNode) {
    return node.expandable;
  }

  /** Get the children for the node. */
  getChildren(node: FileNode) {
    return of(node.children);
  }

  /** Get whether the node has children or not. */
  hasChild(index: number, node: TreeNode) {
    return node.expandable;
  }

  dispatchActionSuccess(res) {
    this.container.hierarchy = JSON.stringify(res);
    this.loading = false;
    this.hierarchyLoaded = true;
    this.store.dispatch(new containerActions.UpdateContainer(this.container, 'Project Imported'));
  }

  public importGitRepo() {
    this.loading = true;
    this.hide = true;
    if (this.gitRepoType === 'public') {
      this.gitUsername = null;
      this.gitPassword = null;
    }

    this.resourceService.importGitRepository(this.gitUrl, this.gitbranch, this.data.containerID, this.activeProject.dbsourceId, this.activeProject.databaseName, this.gitUsername, this.gitPassword)
      .subscribe(res => {
        this.dispatchActionSuccess(res);
        this.gitError = false;
      },
        error => {
          this.loading = false;
          this.hide = false;
          if (error.status === 401) {
            if (error.error === 4011) {
              this.gitError = true;
            }
          }
        });

  }

  public importZipFile() {
    this.hide = true;
    this.resourceService.importZipFile(this.uploader.queue[0]._file, this.container.id, this.activeProject.dbsourceId, this.activeProject.databaseName)
      .subscribe(
        res => this.dispatchActionSuccess(res),
        err => {
          this.dialogRef.close();
          this.toaster.error(err.error);
        }
      );
  }

  public getBranchsList(gitRepoUrl) {
    this.gitUrlBeforeCheck = this.gitUrl;
    this.loadingBranches = true;
    this.unexpectedErrorMsg = null;
    this.resourceService.getBranchsList(gitRepoUrl, this.gitUsername, this.gitPassword)
      .subscribe((branchsList: string[]) => {
        this.branchsList = branchsList;
        this.loadingBranches = false;
        this.gitbranch = branchsList[0];
        this.checksuccess = true;
        this.gitError = false;
      },
        error => {
          this.loadingBranches = false;
          this.checksuccess = false;
          this.hide = false;
          if (error.status === 401) {
            if (error.error === 4011) {
              this.gitError = true;
              this.unexpectedErrorMsg = error.error.error.substring(error.error.lastIndexOf(':') + 1).toUpperCase();
            }
          }
        });
  }

  // Display only file name without path
  public display(name: string) {
    return name.substring(name.lastIndexOf('\\') + 1, name.length);
  }

  // Toggle Import Sections
  toggleGitSection() {
    this.showGitSection = true;
    this.showZipSection = false;
  }

  // Toggle Git Advanced Section
  toggleGitRepoType() {
    if (this.gitRepoType === 'public') {
      this.gitRepoType = 'private';
    } else {
      this.gitRepoType = 'public';
    }
  }

  // Show "Check" Btn if the GitRepoUrl change
  showCheckBtn() {
    this.checksuccess = false;
  }

  openConfirmDeleteFiles() {
    this.dialogRef.addPanelClass('hidden-modal');
    this.messageBoxService.openWarning('popups.resource.deleteAllFiles.title', 'popups.resource.deleteAllFiles.msg', { containerName: this.container.name })
      .afterClosed().subscribe(res => {
        if (res) {
          this.store.dispatch(new containerActions.DeleteActiveContainerFiles(this.container.id));
          this.dialogRef.close();
        } else {
          this.dialogRef.removePanelClass('hidden-modal');
        }
      });
  }

  deleteFiles() {
    console.log(this.container);
    this.resourceService.deleteFiles(this.container.id).subscribe(() => console.log('filesDeleted'));
  }
}

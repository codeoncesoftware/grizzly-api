import { Component, OnInit, Input, Output, EventEmitter, QueryList, ViewChildren, ChangeDetectorRef, DoCheck, AfterViewInit } from '@angular/core';
import { FlatTreeControl } from '@angular/cdk/tree';
import { MatCheckbox } from '@angular/material/checkbox';
import { MatTreeFlattener, MatTreeFlatDataSource, MatTree, MatTreeNode } from '@angular/material/tree';

import { MatDialog } from '@angular/material/dialog';

import { of, Observable } from 'rxjs';
import { Store } from '@ngrx/store';
import * as containerActions from '../../store/container/container.actions';
import { ContainerState } from 'src/app/store/container/container.state';
import { ResourceService } from 'src/app/resource/resource.service';
import { environment } from 'src/environments/environment';
import { Container } from 'src/app/shared/models/Container';
import { ToastrService } from 'ngx-toastr';
import { ImportModalComponent } from '../import-modal/import-modal.component';
import { FileNode } from 'src/app/shared/models/FileNode';
import { ConfirmModalComponent } from 'src/app/shared/confirm-modal/confirm-modal.component';
import { ImportModalService } from '../import-modal/impor-modal.service';


/** Flat node with expandable and level information */
export interface TreeNode {
  name: string;
  type: string;
  level: number;
  fileId: string;
  expandable: boolean;
}

@Component({
  selector: 'app-file-explorer',
  templateUrl: './file-explorer.component.html',
  styleUrls: ['./file-explorer.component.scss']
})

export class FileExplorerComponent implements OnInit, AfterViewInit {
  /** To read the blob as Text */
  reader = new FileReader();
  /** Store file result (resource text) */
  blobb: string;
  /** List of checkboxs */
  @ViewChildren(MatCheckbox) checkboxs: QueryList<MatCheckbox>;
  selectedFileName = '';
  secondaryFiles: any[] = [];
  /** Detect if the component is colled to import a resource */
  @Input() import = false;
  @Input() explorer;
  @Input() selectedFileIdSelected;
  @Input() selectedFileNameSelected;
  @Input() secondaryFilesSelected;
  @Input() activeContainer;
  @Input() selected: string;
  @Input() showSelectBtn: boolean;
  /** Emit selected fileId */
  @Output() notifySelected: EventEmitter<any> = new EventEmitter<any>();
  @Output() notifySelectedSecondary: EventEmitter<any> = new EventEmitter<any>();

  chkbx: any;
  /** Hierarchy */
  treeControl: FlatTreeControl<TreeNode>;
  /** The TreeFlattener is used to generate the flat list of items from hierarchical data. */
  treeFlattener: MatTreeFlattener<FileNode, TreeNode>;
  /** The MatTreeFlatDataSource connects the control and flattener to provide data. */
  dataSource: MatTreeFlatDataSource<FileNode, TreeNode>;

  dataSourceCopy: any;

  files: FileNode[] = [];
  url: string = environment.url;
  container: Container;

  oldDataSource: FileNode[];

  constructor(private toaster: ToastrService,
              public dialog: MatDialog,
              private resourceService: ResourceService,
              private importModalService: ImportModalService,
              private store: Store<ContainerState>, ) {
    // Show File to Edit
    this.reader.addEventListener('loadend', (e) => console.log((e.target as FileReader).result));
    // Hierarchy
    this.treeFlattener = new MatTreeFlattener(
      this.transformer,
      this.getLevel,
      this.isExpandable,
      this.getChildren);

    this.treeControl = new FlatTreeControl<TreeNode>(this.getLevel, this.isExpandable);
    this.dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);
    this.dataSource.data = this.files;
    this.oldDataSource = Object.assign({}, this.dataSource.data);
    this.dataSourceCopy = Object.assign({}, this.dataSource.data[0]);
  }

  ngOnInit() {
    this.store.select('containers').subscribe(res => {
      // tslint:disable-next-line: no-string-literal
      this.container = res['active'];
      this.files = [];
      // tslint:disable-next-line: no-string-literal
      if (res['active'].hierarchy && this.container.hierarchy) {
        this.files.push(JSON.parse(this.container.hierarchy));
        this.dataSource.data = this.files;
        this.oldDataSource = Object.assign({}, this.dataSource.data);
        this.dataSourceCopy = Object.assign({}, this.dataSource.data[0]);
        // Expand the first Node
        this.treeControl.expand(this.treeControl.dataNodes[0]);
      } else {
        this.files = [];
        this.dataSource.data = this.files;
        this.oldDataSource = null;
        this.dataSourceCopy = null;
      }
    });

  }

  ngAfterViewInit(): void {

    if (this.explorer === 1) {
      this.checkboxs.forEach(item => {
        let id1 = item.id;
        id1 = id1.split('\\').join('/');
        id1 = id1.substr(id1.lastIndexOf(this.activeContainer) + this.activeContainer.length + 1, id1.length);
        if (id1 === this.selectedFileNameSelected) {
          item.checked = true;
        }
      });
    } else {
      this.checkboxs.forEach(item => {
        let id2 = item.id;
        id2 = id2.split('\\').join('/');
        id2 = id2.substr(id2.lastIndexOf(this.activeContainer) + this.activeContainer.length + 1, id2.length);
        this.secondaryFilesSelected.forEach(secFile => {
          if (id2 === secFile.fileUri) {
            item.checked = true;
          }
        });
      });
    }


  }

  /** Intercept Checkbox Click to allow only one checkbox checked
   * Notify the parent component about the checked resource file
   */

  public OnChange(node, explorer) {
    console.log(node);
    if (this.explorer === 1) {
      if (this.display(this.selectedFileName) === '') {
        this.selectedFileName = node.name;
        this.notifySelected.emit(node.fileId + '#' + node.name);
        this.checkboxs.forEach(item => {
          if (item.id === node.name) {
            item.checked = true;
          }
        });
      } else if (this.display(this.selectedFileName) === this.display(node.name)) {
        this.notifySelected.emit('');
        this.checkboxs.forEach(item => {
          if (item.id === node.name) {
            item.checked = false;
          }
        });
        this.selectedFileName = '';
      } else {
        this.notifySelected.emit(node.fileId + '#' + node.name);
        this.checkboxs.forEach(item => {
          if (item.id === node.name) {
            item.checked = true;
          } else {
            item.checked = false;
          }
        });
        this.selectedFileName = node.name;
      }
    } else {
      this.checkboxs.forEach(item => {
        if (item.id === (explorer + node.name)) {
          if (item.checked) {
            this.secondaryFiles.push(node.fileId + '#' + node.name);
          } else {
            const resourceIndex = this.secondaryFiles.findIndex(x => x === (node.fileId + '#' + node.name));
            this.secondaryFiles.splice(resourceIndex, 1);
          }
        }
        this.notifySelectedSecondary.emit(this.secondaryFiles);
      });
    }
  }

  /** Uncheck All Checkboxs */
  public uncheckAll(): void {
    this.checkboxs.forEach(item => item.checked = false);
    this.selectedFileName = '';
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
  getLevel(node: TreeNode): number {
    return node.level;
  }

  /** Return whether the node is expanded or not. */
  isExpandable(node: TreeNode): boolean {
    return node.expandable;
  }

  /** Get the children for the node. */
  getChildren(node: FileNode): Observable<FileNode[]> {
    return of(node.children);
  }

  /** Get whether the node has children or not. */
  hasChild(index: number, node: TreeNode): boolean {
    return node.expandable;
  }

  /** Display only file name without path */
  public display(name: string): string {
    if (name) {
      return name.lastIndexOf('\\') > - 1 ? name.substring(name.lastIndexOf('\\') + 1, name.length) : name.substring(name.lastIndexOf('\/') + 1, name.length);
    }
  }

  /** Get resource File */
  public copyFileLink(node: TreeNode): void {
    let fileUri = '';
    if (node.name.includes(this.container.id)) {
      fileUri = node.name.substring(node.name.indexOf(this.container.id) + this.container.id.length + 1);
    } else {
      fileUri = node.name;
    }
    const url: string = this.url + 'runtime/static/' + this.container.id + '/path/' + fileUri;
    const re: RegExp = /\\/gi;
    const selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = url.replace(re, '/');
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
    this.toaster.success('Url copied to clipboard');
  }

  concat(x: string, y: string): string {
    return x + y;
  }

  importFilesIfEmpty() {
    this.dialog.closeAll();
    this.importModalService.openImport({
      containerID: this.container.id,
      action: {
        confirm: false,
        msg: 'Import Project'
      }
    });
  }

  selectFile(file: FileNode) {
    this.notifySelected.emit(file);
  }

  onSearchChange(searchValue: string): void {
    const results: FileNode[] = [];
    this.files = [];
    if (searchValue.length === 0 && this.oldDataSource) {
      this.files.push(this.dataSourceCopy);
    } else {
      this.getObject(Object.assign({}, this.oldDataSource)[0], searchValue, results);
      results.filter((v, i, a) => a.findIndex(t => (JSON.stringify(t) === JSON.stringify(v))) === i).forEach(element => this.files.push(element));
    }
    if (this.files.length > 0) {
      this.dataSource.data = this.files;
    } else {
      this.files = [];
      this.files.push(this.dataSourceCopy);
      this.dataSource.data = this.files;
    }
  }

  getObject(fileNode: FileNode, key: string, results: FileNode[]) {
    let result = null;
    if (fileNode.children && fileNode.children.length > 0) {
      for (const item of fileNode.children) {
        result = this.getObject(item, key, results);
        if (result) {
          break;
        }
      }
    } else {
      for (const prop of Object.keys(fileNode)) {
        if (this.display(fileNode.name).toUpperCase().includes(key.toUpperCase()))  {
          results.push(fileNode);
        }

        if (fileNode[prop] instanceof Object || fileNode[prop] instanceof Array) {
          result = this.getObject(fileNode[prop], key, results);
          if (result) {
            break;
          }
        }
      }
    }
    return result;
  }


  filtering(fileNode: FileNode, key: string) {
    if (fileNode.type === 'file') {
      console.log(fileNode);
    }

    for (const item of fileNode.children) {
      this.filtering(item, key);
    }
  }

  deleteFile(fileId: string) {
    const dialogRef = this.dialog.open(ConfirmModalComponent,
      {
        width: '40%',
        position: {
          top: '15vh'
        },
        data: {
          action: {
            msg: 'popups.group.deleteAll'
          }
        }
      });
    dialogRef.afterClosed().subscribe(res => {
      if (res) {
        this.resourceService.deleteFile(this.container.id, fileId).subscribe(ress => {
          this.store.dispatch(new containerActions.UpdateContainer(this.container, 'File Deleted'));
        });

      }
    });
  }


}

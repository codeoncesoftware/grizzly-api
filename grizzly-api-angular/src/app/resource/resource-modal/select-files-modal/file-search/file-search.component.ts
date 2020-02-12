import { Component, OnInit, Output, EventEmitter, ViewChild, Input } from '@angular/core';
import { FileNode } from 'src/app/container/import-modal/import-modal.component';
import { Store } from '@ngrx/store';
import * as containerActions from '../../../../store/container/container.actions';
import { ContainerState } from 'src/app/store/container/container.state';
import { MatSelectionList } from '@angular/material';
import { FileExplorerComponent } from 'src/app/container/file-explorer/file-explorer.component';
import { SelectFilesModalService } from '../select-files-modal.service';
import { SelectedFile } from 'src/app/shared/models/SelectedFile';
import { FileUploader } from 'ng2-file-upload';
import { ResourceService } from 'src/app/resource/resource.service';
import { Container } from 'src/app/shared/models/Container';

@Component({
  selector: 'app-file-search',
  templateUrl: './file-search.component.html',
  styleUrls: ['./file-search.component.scss']
})
export class FileSearchComponent implements OnInit {

  @Input() selectionMode: string;
  @Output() selectedFile = new EventEmitter<SelectedFile>();

  @ViewChild('files', { static: false }) selectList: MatSelectionList;
  @ViewChild('tree', { static: false }) fileExplorer: FileExplorerComponent;

  files: FileNode[] = [];
  container: Container;
  /** Name of Hierarchy Root */
  rootFolderName: string;
  /** After Filtering Files */
  results: FileNode[] = [];
  /** Search Key */
  searchValue: string;
  /** Show Tree Bar */
  showTree = true;

  public uploader: FileUploader = new FileUploader({});

  constructor(private store: Store<ContainerState>,
              public selectFileService: SelectFilesModalService,
              private resourceService: ResourceService) { }

  ngOnInit() {
    this.store.select('containers').subscribe(res => {
      // tslint:disable-next-line: no-string-literal
      this.container = res['active'];
      this.files = [];
      // tslint:disable-next-line: no-string-literal
      if (res['active'].hierarchy && this.container['hierarchy'] !== 'none') {
        // tslint:disable-next-line: no-string-literal
        this.rootFolderName = this.display(JSON.parse(this.container['hierarchy']).name);
        // tslint:disable-next-line: no-string-literal
        this.getFiles(JSON.parse(this.container['hierarchy']), this.results);
        if (this.results) {
          this.results = [...new Set(this.results)];
          this.results.filter((v, i, a) => a.findIndex(t => (JSON.stringify(t) === JSON.stringify(v))) === i).forEach(element => {
            element.name = element.name.substring(element.name.indexOf(this.rootFolderName) + this.rootFolderName.length + 1);
            this.files.push(element);
          });
          this.results.forEach(element => this.files.push(element));
          this.results = [];
        }
      }
    });
  }


  getFiles(fileNode: FileNode, results: FileNode[]) {
    let result = null;
    if (fileNode.children && fileNode.children.length > 0) {
      for (const item of fileNode.children) {
        result = this.getFiles(item, results);
        if (result) {
          break;
        }
      }
    } else {
      for (const prop of Object.keys(fileNode)) {
        if (fileNode.type === 'file') {
          results.push(fileNode);
        }

        if (fileNode[prop] instanceof Object || fileNode[prop] instanceof Array) {
          result = this.getFiles(fileNode[prop], results);
          if (result) {
            break;
          }
        }
      }
    }
    return result;
  }

  /** Display only file name without path */
  public display(name: string): string {
    if (name) {
      return name.lastIndexOf('\\') > - 1 ? name.substring(name.lastIndexOf('\\') + 1, name.length) : name.substring(name.lastIndexOf('\/') + 1, name.length);
    }
  }

  onSearchChange(searchValue: string): void {
    if (searchValue.length < 1) {
      this.results = [];
    } else {
      this.results = this.files.filter(item => item.name.toUpperCase().includes(searchValue.toUpperCase()));
    }
    this.fileExplorer.onSearchChange(searchValue);
  }

  selectFile(file: FileNode) {
    // this.selectedFile.emit(new SelectedFile(file, this.selectionMode));
    this.selectFileService.selectedFile.emit(new SelectedFile(file, this.selectionMode));
    this.results = [];
    this.searchValue = '';
  }

  showFilesTree() {
    this.showTree = !this.showTree;
  }

  public importFiles() {
    this.uploader.queue.forEach(file => {
      this.resourceService.saveFile(file._file, this.container.id).subscribe((res) => this.store.dispatch(new containerActions.GetContainer(this.container.id, 'Imported')));
    });

  }

}

import { Component, OnInit, Inject, ViewChild } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { FileNode } from 'src/app/container/import-modal/import-modal.component';

@Component({
  selector: 'app-select-files-modal',
  templateUrl: './select-files-modal.component.html',
  styleUrls: ['./select-files-modal.component.sass']
})
export class SelectFilesModalComponent implements OnInit {

  /** Array to Share Data Between search-file Component and upload-fie-component */
  searchSelectedFiles: FileNode[] = [];
  activecontainerId: string;
  showTree = false;
  showSelectBtn = false;

  constructor(public dialog: MatDialog,
              public selectFilesModalDialogRef: MatDialogRef<SelectFilesModalComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) { }

  ngOnInit() {
    this.activecontainerId = this.data.activecontainerId;
  }

  addToSelectedFile(file: FileNode) {
    if (this.searchSelectedFiles.findIndex(item => item.fileId === file.fileId) < 0) {
      this.searchSelectedFiles.push(file);
    }
  }

  showFilesTree() {
    this.showTree = !this.showTree;
    this.showSelectBtn = true;
  }

}

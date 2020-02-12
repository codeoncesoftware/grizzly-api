import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { ContainerService } from '../container.service';
import { FileItem, FileUploader } from 'ng2-file-upload';
import { map, catchError } from 'rxjs/operators';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-swagger-url',
  templateUrl: './swagger-url.component.html',
  styleUrls: ['./swagger-url.component.scss']
})
export class SwaggerUrlComponent implements OnInit {

  swaggerUrl: string;
  @Input() uploader: FileUploader;
  @Output() fileLinked = new EventEmitter<FileItem>();
  @Output() undo = new EventEmitter();

  showInvalidUrl = false;

  constructor(private containerService: ContainerService) { }

  ngOnInit() {
  }

  emitUndo() {
    this.undo.emit();
  }

  getSwaggerFromUrl(swaggerUrl) {
    if (swaggerUrl) {
      this.containerService.getSwaggerFileFromUrl(swaggerUrl).subscribe(
        (content) => {
          this.showInvalidUrl = false;
          const data = new Blob([content], { type: 'text' });
          const arrayOfBlob = new Array<Blob>();
          arrayOfBlob.push(data);
          const swaggerFile = new File(arrayOfBlob, 'swagger');
          const file = new FileItem(this.uploader, swaggerFile, null);
          this.fileLinked.emit(file);
        },
        (err) => this.showInvalidUrl = true);
    }
  }

}

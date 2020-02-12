import { FileNode } from './FileNode';

export class SelectedFile {
    file: FileNode;
    mode: string;

    constructor(file?: FileNode, mode?: string) {
        this.file = file;
        this.mode = mode;
    }
}

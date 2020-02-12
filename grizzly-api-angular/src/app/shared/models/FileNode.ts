/** File node data with nested structure. */
export class FileNode {
    name: string;
    type: string;
    fileId: string;
    children?: FileNode[];
}

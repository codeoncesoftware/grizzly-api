export class ResourceFile {
    fileId: string;
    fileUri: string;

    constructor(fileId?: string, fileUri?: string) {
        this.fileId = fileId;
        this.fileUri = fileUri;
    }
}

export class ResourceGroupLayout {
    containerId: string;
    openGroupsIndexs: number[] = [];

    constructor(contId: string, index: number) {
        this.containerId = contId;
        this.openGroupsIndexs.push(index);
    }
}

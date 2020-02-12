import { Resource } from './Resource';

export class ResourceGroup {
    name: string;
    description: string;
    resources: Resource[] = [];

    constructor(name?: string) {
        this.name = name;
    }
}

import { ResourceGroup } from './ResourceGroup';

// DTO Container
export class Container {
    id: string;
    projectId: string;
    name: string;
    description: string;
    creationTime: string;
    lastUpdate: string;
    resourceGroups: ResourceGroup[] = [];
    hierarchy = '';
    enabled: boolean;
    swaggerUuid: string;
}

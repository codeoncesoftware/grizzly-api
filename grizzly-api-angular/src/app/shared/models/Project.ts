import { Security } from './Security';

// DTO Project
export class Project {
    id: string;
    name: string;
    creationTime: string;
    lastUpdate: string;
    description: string;
    userEmail: string;
    dbsourceId: string;
    databaseName: string;
    securityConfig: Security = new Security();
    roles: string[];
}

import { Database } from './Database';

export class DBSource {
    id: string;
    name: string;
    description: string;
    host: string;
    port: number;
    uri: string;
    database: string;
    userEmail: string;
    authenticationDatabase: string;
    gridFsDatabase: string;
    username: string;
    password: string;
    creationTime: string;
    lastUpdate: string;
    active: boolean;
    databases: Database[];
    collectionsList: string[];
    connectionMode: string;
    provider: string;
    connectionSucceeded: boolean;
    authRequired: boolean;

    constructor() {
        this.port = 27017;
        this.connectionMode = 'FREE';
        this.provider = 'mongo';
        this.authenticationDatabase = 'admin';
    }
}

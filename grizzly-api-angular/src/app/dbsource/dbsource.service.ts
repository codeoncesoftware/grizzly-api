import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DBSource } from '../shared/models/DBSource';
import { environment } from 'src/environments/environment';
import { Observable } from 'rxjs';


@Injectable({
    providedIn: 'root'
})

export class DBSourceService {

    private baseUrl: string = environment.baseUrl + '/api/dbsource';

    constructor(private http: HttpClient) { }

    /**
     * Check whether the given information is correct or not
     * @param dbSource The details of the new DataSource to Create
     */
    public checkConnection(dbSource: DBSource): Observable<boolean> {
        return this.http.post<boolean>(this.baseUrl + '/check', dbSource);
    }

    /**
     * Check whether the given name is unique or not
     * @param dbSource The details of the new DataSource to Create
     */
    public checkUnicity(dbSourceName: string, dbSourceId: string): Observable<boolean> {
        return this.http.get<boolean>(this.baseUrl + '/check/name/' + dbSourceName + '/' + dbSourceId);
    }

    /**
     * Save a DBSource Instance in Database
     * @param dbSource the new user Input
     */
    public saveDBSource(dbSource: DBSource): Observable<DBSource> {
        return this.http.post<DBSource>(this.baseUrl + '/create', dbSource);
    }

    /**
     * Retrieve a DBSource Instance from a DB based on the Given Id
     * @param dbSourceId (If of the DBSource to retrieve)
     */
    public getDBSourceById(dbSourceId: string): Observable<DBSource> {
        return this.http.get<DBSource>(this.baseUrl + '/' + dbSourceId);
    }

    /**
     * Retrieve all Datasources from DB
     */
    public getAll() {
        return this.http.get<DBSource[]>(this.baseUrl + '/all');
    }

    /**
     * Delete a Datasource based on the Given Id
     * @param dbsourceId : ID of the Datasource to Delete
     */
    public deleteDBSource(dbsourceId: string) {
        return this.http.delete(this.baseUrl + '/delete/' + dbsourceId);
    }

    public getCollectionStats(dbsourceId: string, databaseName: string, collectionName: string) {
        return this.http.get(this.baseUrl + '/stats/' + dbsourceId + '/' + databaseName + '/' + collectionName);
    }

    public dropCollection(dbsourceId: string, databaseName: string, collectionName: string) {
        return this.http.delete(this.baseUrl + '/drop/' + dbsourceId + '/' + databaseName + '/' + collectionName);
    }

    /** Add a New Collection To A Database */
    public saveNewCollection(containerId: string, collectionName: string): Observable<boolean> {
        return this.http.post<boolean>(this.baseUrl + '/addcollection/' + containerId + '/' + collectionName, {});
    }
}

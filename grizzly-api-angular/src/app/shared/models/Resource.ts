import { Parameter } from './Parameter';
import { APIResponse } from './APIResponse';
import { CustomQuery } from './CustomQuery';
import { ResourceFile } from './ResourceFile';

// Resource DTO
export class Resource {
    // Definition
    name: string;
    summary: string;
    description: string;
    // Request
    executionType = 'Query';
    customQuery: CustomQuery = new CustomQuery();
    path = '/';
    httpMethod = 'POST';
    resourceFile = new ResourceFile();
    secondaryFilePaths: ResourceFile[] = [];
    consumes: string[] = ['application/json'];
    parameters: Parameter[] = [];
    creationTime: string;
    // Response
    produces: string[] = ['application/json'];
    securityLevel: string[] = ['public'];
    responses: APIResponse[] = [
        new APIResponse('200', 'Ok'),
        new APIResponse('401', 'Unauthorized'),
        new APIResponse('403', 'Forbidden')
    ];
    // Projection
    fields: string [];
    // Pagination
    pageable = false;

}

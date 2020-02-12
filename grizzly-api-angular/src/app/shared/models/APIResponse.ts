
export class APIResponse {
    code: string;
    description: string;

    constructor(code?: string, desc?: string) {
        this.code = code;
        this.description = desc;
    }
}

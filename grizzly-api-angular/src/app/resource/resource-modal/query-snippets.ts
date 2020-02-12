class Param {
    in: string;
    name: string;
    type: string;
    value: string;

    constructor(inn: string, name: string, type: string, value?: string) {
        this.in = inn;
        this.name = name;
        this.type = type;
        this.value = value;
    }
}

class Snippet {
    query: string;
    params: Param[];

    constructor(query: string, params: Param[]) {
        this.query = query;
        this.params = params;
    }
}

export const Snippets: ReadonlyMap<string, Snippet> = new Map([
    ['findByFirstname', new Snippet('{"firstName": "$session_firstname"}', [])],
    ['findByLastname', new Snippet('{"lastName": "$session_lastname"}', [])],
    ['findByCurrentUserUsername', new Snippet('{"username": "$session_username"}', [])],
    ['findByCurrentUserEmail', new Snippet('{"email": "$session_email"}', [])],
    ['findByCurrentUserPhone', new Snippet('{"username": "$session_phone"}', [])],
    ['findByAgeLessThan', new Snippet('{"age": {"$lt": "%age"}}', [new Param('Query', 'age', 'Integer')])],
    ['findByAgeGreaterThan', new Snippet('{"age": {"$gt": "%age"}}', [new Param('Query', 'age', 'Integer')])],
]);

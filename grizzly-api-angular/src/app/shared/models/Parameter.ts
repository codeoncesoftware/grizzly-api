export class Parameter {
    name: string;
    type = 'String';
    value: string;
    in = 'Body';

    constructor(inn?: string, name?: string, type?: string, value?: string) {
        this.in = inn;
        this.name = name;
        this.type = type;
        this.value = value;
    }
}

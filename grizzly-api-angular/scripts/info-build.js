'use strict';

//MODULES
const fs = require('fs');

//READ PACKAG.JSON
let packageFile = fs.readFileSync('package.json');
let packageData = JSON.parse(packageFile);

// BUILD INFO OBJECT
let buildInfo = {};
buildInfo.app = packageData.name;
buildInfo.version = packageData.version;
buildInfo.date = new Date();

// BUILD INFO TS FILE
let buildInfoTsData = 'export const buildInfo =' + JSON.stringify(buildInfo) + ';'
console.log(buildInfoTsData);

// CREATE GENERATED FOLDER IF IT DOESN'T EXIST
fs.mkdir('src/generated/', { recursive: true }, (err) => {
    if (err) throw err;
});

// WRITE FILE
fs.writeFileSync('src/generated/build-info.ts', buildInfoTsData);
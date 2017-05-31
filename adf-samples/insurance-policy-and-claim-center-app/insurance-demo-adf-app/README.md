# insurance-demo-adf-app

Alfresco Insurance Policy and Claim Center ADF Demo

## Prerequisites

Before you start using this development framework, make sure you have installed all required software and done all the
necessary configuration, see this [page](https://github.com/Alfresco/alfresco-ng2-components/blob/master/PREREQUISITES.md).

## Building and Running

Install dependencies

```sh
npm install
```

### Development build

```sh
npm start
```

This command compiles and starts the project in watch mode.
Browser will automatically reload upon changes.
Upon start you can navigate to `http://localhost:3000` with your preferred browser.

#### Important notes

This script is recommended for development environment and not suited for headless servers and network access.

### Production build

```sh
npm run build
npm run start:dist
```

This command builds project in `production` mode.
All output is placed to `dist` folder and can be served your preferred web server.
You should need no additional files outside the `dist` folder.

#### Important notes

By default demo application is configured to use [wsrv](https://www.npmjs.com/package/wsrv) tool (lightweight web server)
to serve production build output. It will be running at `0.0.0.0` address with port `3000` and allow you accessing your application
via network. However you can use any web server of your choice in production.


## Custom-files

If you need to add custom files on your project you can add this files in the folders public

```
.
├── public/
│   ├── images/
│   ├── css/
│   └── js/
```

the public folder above wil be copied in the root of your project and you can refer to them for example as

 * './images/custom_image.png'
 * './js/custom_script.js'
 * './css/custom_style.css'
  

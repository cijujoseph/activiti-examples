## Implementation of a Rich text Editor Form Stencil in Alfresco Process Service's OOTB UI. This is based on tinymce editor (https://www.tinymce.com) & its angularjs code base (https://github.com/angular-ui/ui-tinymce)

### How to run
1. Copy "tinymce" folder to /webapps/activiti-app/libs folder. This is a snapshot of tinymce libs. If you need the latest, download from https://www.tinymce.com/download/.
2. Add the following  code block to /webapps/activiti-app/scripts/app-cfg.js to load the tinymce scripts. 
	`ACTIVITI.CONFIG.resources = {
    'workflow': [
        {
            'tag': 'script',
            'type': 'text/javascript',
            'src': ACTIVITI.CONFIG.webContextRoot + '/libs/tinymce/tinymce.js?v=1.0'
        }
    ]
}`
3. Import the app zip file and run it in APS.

A screenshot of the form given below

![alt tag]( https://github.com/cijujoseph/activiti-examples/blob/master/stencil-samples/tinymce-rich-text-editor/form-screenshot.jpg )


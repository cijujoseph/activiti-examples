### Deployment of the Admin application of Alfresco Process Services (APS) on Pivotal Cloud Foundry (PCF)
This is an example project showing how to repackage the activiti-admin.war of Alfresco Process Services (powered by Activiti) with customer specific configurations and also with PCF specific configurations. This has been tested with APS v1.8 in a PCF Development environment, specifically PCFDev v0.28.0

The packaging & deployment approach here is very similar to the deployment of APS App which I have explained in great detail at [Deployment of Alfresco Process Services (APS) on Pivotal Cloud Foundry (PCF)](https://github.com/cijujoseph/activiti-examples/tree/master/activiti-app-pcf).

> If you trying to run both [activiti-app](https://github.com/cijujoseph/activiti-examples/tree/master/activiti-app-pcf) & [activiti-admin](https://github.com/cijujoseph/activiti-examples/tree/master/activiti-admin-pcf) in a PCFDev environment on local machine, memory setting could be a challenge. A configuration that worked for me is:
>
* activiti-app: 1300M
* activiti-admin: 750M
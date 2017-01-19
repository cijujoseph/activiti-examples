## Demo showing how to build a custom business day calculator which can be used on Task Due Date, timers etc
The an example usage is ${businessDaysCalculator.businessDueDateCalculator("P3D")}. This will calculate 3 business days from today. This can be modified/extended to meet the needs

I used http://www.kayaposoft.com/enrico/json/ to generate the holiday json file in this example.
eg:
http://www.kayaposoft.com/enrico/json/v1.0/?action=getPublicHolidaysForDateRange&fromDate=04-07-2012&toDate=04-07-2040&country=usa

mvn clean package will generate the zip and jar files. zip can be imported in activiti-app. jar can be put in the lib folder. example demonstrated in the sample app available at src/main/app folder.

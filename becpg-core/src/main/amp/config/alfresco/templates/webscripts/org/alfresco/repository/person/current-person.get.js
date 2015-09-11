function main()
{
   // Do we want to return containing groups?
   var groups = (args["groups"] == "true");
   model.userName = person.properties.userName;
   model.groups = people.getContainerGroups(person);
}

main();

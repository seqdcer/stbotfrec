//
// This script setups the tech "tree" for the given race.
//

// set tech levels if present otherwise load them
if (scriptArgs.length > 1)
{
    CI.run("createList", "user/races/" + scriptArgs[0] + "/techLevel");
    
    for (i = 1; i < scriptArgs.length; i++)
    {
        CI.run("addToList", "user/races/" + scriptArgs[0] + "/techLevel", scriptArgs[i]);
    }
}
else
{
    techcount = CI.run("getVariable", "user/races/" + scriptArgs[0] + "/techLevel/count");
    
    for (i = 0; i < techcount; i++)
    {
        scriptArgs.push(CI.run("getVariable", "user/races/" + scriptArgs[0] + "/techLevel/" + i));
    }
}

// empire or minor race ?
empireCount = parseInt(CI.run("getVariable", "static/stats/empires/list/count"));
isEmpire = parseInt(scriptArgs[0]) < empireCount;

// setup ships
if (isEmpire === true)
{
    ships = CI.run("getVariable", "static/stats/races/[static/stats/empires/list/" + scriptArgs[0] + "]/shiplist");
}
else
{
    ships = CI.run("getVariable", "static/stats/races/[static/stats/minors/list/" + (parseInt(scriptArgs[0]) - empireCount) + "]/shiplist");
}

if (ships !== null)
{
    CI.run("createList", "user/races/" + scriptArgs[0] + "/tech/future/ships");
    
    for (i = 0; i < ships.size(); i++)
    {
        CI.run("addToList", "user/races/" + scriptArgs[0] + "/tech/future/ships", ships.get(i));
    }
}
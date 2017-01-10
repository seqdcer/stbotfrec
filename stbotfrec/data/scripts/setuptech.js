//
// This script setups the tech "tree" for the given race.
//
// Call this on new game and game load.
//
// Args: race index, optional: [tech levels]

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
    var techcount = CI.run("getVariable", "user/races/" + scriptArgs[0] + "/techLevel/count");
    
    for (i = 0; i < techcount; i++)
    {
        scriptArgs.push(CI.run("getVariable", "user/races/" + scriptArgs[0] + "/techLevel/" + i));
    }
}

// empire or minor race ?
var empireCount = parseInt(CI.run("getVariable", "static/stats/empires/list/count"));
var isEmpire = parseInt(scriptArgs[0]) < empireCount;
var ships = null;

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
    CI.run("createList", "runtime/races/" + scriptArgs[0] + "/futuretech/ships");
    
    for (i = 0; i < ships.size(); i++)
    {
        CI.run("addToList", "runtime/races/" + scriptArgs[0] + "/futuretech/ships", ships.get(i));
    }
}
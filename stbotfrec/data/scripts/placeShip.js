//
// This script places a new ship to the galaxy map.
//
// Args: shipid, race index, system index

// get system location
var x = CI.run("getVariable", "user/galaxy/systems/" + scriptArgs[2] + "/x");
var y = CI.run("getVariable", "user/galaxy/systems/" + scriptArgs[2] + "/y");

// get next tf id
var nextId = CI.run("getVariable", "user/races/" + scriptArgs[1] + "/tfs/nextId");

if (nextId === null)
{
    nextId = 0;
}

nextId = parseInt(nextId);

while (CI.run("getVariable", "user/races/" + scriptArgs[1] + "/tfs/list/" + nextId) !== null)
{
    nextId = nextId + 1;
}

// create taskforce
CI.run("createList", "user/races/" + scriptArgs[1] + "/tfs/list/" + nextId + "/ships");

CI.run("setVariable", "user/races/" + scriptArgs[1] + "/tfs/list/" + nextId + "/ships/" + 0 + "/ship", scriptArgs[0]);
CI.run("setVariable", "user/races/" + scriptArgs[1] + "/tfs/list/" + nextId + "/ships/" + 0 + "/hull", "static/stats/ships/" + scriptArgs[0] + "/hull");
CI.run("setVariable", "user/races/" + scriptArgs[1] + "/tfs/list/" + nextId + "/ships/" + 0 + "/xp", 0);
CI.run("setVariable", "user/races/" + scriptArgs[1] + "/tfs/list/" + nextId + "/ships/" + 0 + "/level", 0);
CI.run("setVariable", "user/races/" + scriptArgs[1] + "/tfs/list/" + nextId + "/mapSpeed", "static/stats/ships/" + scriptArgs[0] + "/mapSpeed");
CI.run("setVariable", "user/races/" + scriptArgs[1] + "/tfs/list/" + nextId + "/mapRange", "static/stats/ships/" + scriptArgs[0] + "/mapRange");

// add taskforce to sector
if (CI.run("getVariable", "user/galaxy/map/" + x + "/" + y + "/tfs/" + scriptArgs[1]) === null)
{
    CI.run("createList", "user/galaxy/map/" + x + "/" + y + "/tfs/" + scriptArgs[1]);
}

CI.run("addToList", "user/galaxy/map/" + x + "/" + y + "/tfs/" + scriptArgs[1], nextId);

// add to minors list if needed
var empireCount = CI.run("getVariable", "static/stats/empires/list/count");
var racesCount = CI.run("getVariable", "user/races/count");

if (parseInt(scriptArgs[1]) >= parseInt(empireCount) && parseInt(scriptArgs[1]) < parseInt(racesCount))
{
    if (CI.run("getVariable", "user/galaxy/map/" + x + "/" + y + "/tfs/minors") === null)
    {
        CI.run("createList", "user/galaxy/map/" + x + "/" + y + "/tfs/minors");
    }
    
    CI.run("addToList", "user/galaxy/map/" + x + "/" + y + "/tfs/minors", nextId);
}

// add to aliens list of needed
if (parseInt(scriptArgs[1]) >= parseInt(racesCount))
{
    if (CI.run("getVariable", "user/galaxy/map/" + x + "/" + y + "/tfs/aliens") === null)
    {
        CI.run("createList", "user/galaxy/map/" + x + "/" + y + "/tfs/aliens");
    }
    
    CI.run("addToList", "user/galaxy/map/" + x + "/" + y + "/tfs/aliens", nextId);
}

// update nextId
nextId = nextId + 1;
CI.run("setVariable", "user/races/" + scriptArgs[1] + "/tf/nextId", nextId);

CI.run("debugPrint", "Added ship: " + scriptArgs[0] + ": " + scriptArgs[1] + ": " + scriptArgs[2]);
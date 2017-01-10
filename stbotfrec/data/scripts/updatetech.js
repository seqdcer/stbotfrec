//
// This script checks tech requirement for all future tech and moves appropriate items to current tech tree.
//
// Args: raceid

// get tech levels
var techcount = CI.run("getVariable", "user/races/" + scriptArgs[0] + "/techLevel/count");
var techLevels = [];
    
for (i = 0; i < techcount; i++)
{
    techLevels.push(CI.run("getVariable", "user/races/" + scriptArgs[0] + "/techLevel/" + i));
}

// check ships
var shipTech = CI.run("getVariable", "runtime/races/" + scriptArgs[0] + "/futuretech/ships");

// create current ship tech list if needed
if (CI.run("getVariable", "user/races/" + scriptArgs[0] + "/tech/ships") === null)
{
    CI.run("createList", "user/races/" + scriptArgs[0] + "/tech/ships");
}

for (i = 0; i < shipTech.size(); i++)
{
    // get ship requirements
    var ship = shipTech.get(i);
    
    var techReq = CI.run("getVariable", "static/stats/ships/" + ship + "/techReq")
    var addToCurrent = true;
    
    // check them
    for (t = 0; t < techReq.size() && t < techLevels.length; t++)
    {
        if (techReq.get(t) > techLevels[t])
        {
            addToCurrent = false;
            break;
        }
    }
    
    // add to current tech ?
    if (addToCurrent === true)
    {
        // add only if the ship is not already on the list
        CI.run("addToList", "user/races/" + scriptArgs[0] + "/tech/ships", 0, ship, true);

        // remove from future tech
        CI.run("removeItemFromList", "runtime/races/" + scriptArgs[0] + "/futuretech/ships", ship);
        
        // we need to reduce index since shipTech is a "live" array from the data tree
        // we could remove the ship directly from shipTech as well and it would be reflected internally
        // however that would not fire the proper data change events
        // so always remove using removeItemFromList instead, never change objects directly
        i--;
        
        //TODO: manage upgrades
    }
}

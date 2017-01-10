
if (CI.run("getVariable runtime/gamesetup/playerEmpire") >= 0)
{
    CI.run("showScreen loading");

    var v = new java.lang.Runnable() {
        run: function () {
            // setup new game
            CI.run("setVariable runtime/player/empireSmallPreffix [static/stats/empires/smallPrefixes/[runtime/gamesetup/playerEmpire]]");
            CI.run("setVariable user/player/playerEmpire [runtime/gamesetup/playerEmpire]");
            CI.run("setVariable user/game/turn 1");
            
            var rngSeed = CI.run("getRandomLong");
            CI.run("setVariable", "user/game/startingSeed", rngSeed);
            CI.run("setVariable", "user/game/currentSeed", rngSeed);
            
            // add races
            CI.run("createList user/races");
            
            // add empires
            var empireCount = CI.run("getVariable static/stats/empires/list/count");
            var racesCount = empireCount;
            var empireList = "";
            
            for (rindex = 0; rindex < empireCount; rindex++)
            {
                CI.run("setVariable user/races/" + rindex + "/id [static/stats/empires/list/" + rindex + "]");
                empireList = empireList + "$&[static/text/races/[user/races/" + rindex + "/id]/shortEmpireName&]";
            }
            
            // add minor races
            
            // setup races
            for (rindex = 0; rindex < racesCount; rindex++)
            {
                setupRace(rindex, empireList);
            }
            
            // generate galaxy
            CI.run("runScript", "galaxygen");
            
            // once done display maingal screen
            CI.run("showScreen [runtime/player/empireSmallPreffix]maingal");
        }
    };
    
    var t = new java.lang.Thread(v);
    t.start();
}

function setupRace(rindex, empireList)
{
    // SETUP SUMMARY
    // clear events list
    CI.run("createList user/races/" + rindex + "/summary/events");
    CI.run("setRefVariable", "user/races/" + rindex + "/summary/events/0/text", "static/text/lexicon/UI/summary/eventsListTitle");
    // clear relationships list
    CI.run("createList user/races/" + rindex + "/summary/relations");
    CI.run("setRefVariable", "user/races/" + rindex + "/summary/relations/0/text", "static/text/lexicon/UI/summary/relationshipsListTitle" + empireList);
    // clear systems list
    CI.run("createList user/races/" + rindex + "/summary/systems");
    CI.run("setRefVariable", "user/races/" + rindex + "/summary/systems/0/text", "static/text/lexicon/UI/summary/systemsListTitle");
    // setup list of owned systems
    CI.run("createList user/races/" + rindex + "/systems");
    
    // SETUP TECH TREE
    var tech = CI.run("getVariable static/stats/races/[user/races/" + rindex + "/id]/startingTech/[settings/startingLevels/" + rindex + "/index]");
    var params = rindex;
    
    for (i = 0; i < tech.size(); i++)
    {
        params = params + " " + tech.get(i);
    }
    
    CI.run("runScript setuptech " + params);
    CI.run("runScript", "updatetech", rindex);
}
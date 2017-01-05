// executed when the player hits the turn button

// show turn processing dialog
CI.run("setVariable runtime/txtdialog/text [static/text/lexicon/UI/processingTurn]");
CI.run("showDialog", "[runtime/player/empireSmallPreffix]txtdlg", true);

// run stuff in thread / background
var v = new java.lang.Runnable() {
        run: function () {
        // process turn
        CI.run("modVariable user/game/turn 1");
        
        // process races
        var races = CI.run("getVariable user/races/count");
        var empireCount = CI.run("getVariable static/stats/empires/list/count");
        var empireList = "";

        for (rindex = 0; rindex < empireCount; rindex++)
        {
            empireList = empireList + "$&[static/text/races/[user/races/" + rindex + "/id]/shortEmpireName&]";
        }
            
        for (rindex = 0; rindex < races; rindex++)
        {
            // CLEAR SUMMARY
            // clear events list
            CI.run("createList user/races/" + rindex + "/summary/events");
            CI.run("setRefVariable", "user/races/" + rindex + "/summary/events/0/text", "static/text/lexicon/UI/summary/eventsListTitle");
            // clear relationships list
            CI.run("createList user/races/" + rindex + "/summary/relations");
            CI.run("setRefVariable", "user/races/" + rindex + "/summary/relations/0/text", "static/text/lexicon/UI/summary/relationshipsListTitle" + empireList);
            // clear systems list
            CI.run("createList user/races/" + rindex + "/summary/systems");
            CI.run("setRefVariable", "user/races/" + rindex + "/summary/systems/0/text", "static/text/lexicon/UI/summary/systemsListTitle");
        }

        // process ships
        
        // process systems
        var systems = CI.run("getVariable user/galaxy/systems/count");
        
        for (sysindex = 1; sysindex < systems; sysindex++)
        {
            var inhabited = CI.run("getVariable user/galaxy/systems/" + sysindex + "/inhabited") === 'true';
            var maxPop = parseInt(CI.run("getVariable user/galaxy/systems/" + sysindex + "/maxPop"));
            var pop = parseInt(CI.run("getVariable user/galaxy/systems/" + sysindex + "/pop"));
            
            if (inhabited && pop < maxPop)
            {
                pop = pop + 1;
                CI.run("setVariable user/galaxy/systems/" + sysindex + "/pop " + pop);
            }
        }
        
        for (dindex = 1; dindex < 40; dindex++)
        {
            // test data for events list
            CI.run("setVariable", "user/races/1/summary/events/" + dindex + "/text", "Test data " + dindex);
        }

        // last step: save current rng seed then do an auto-save
        var rngSeed = CI.run("getRandomLong");
        CI.run("setVariable", "user/game/currentSeed", rngSeed);

        CI.run("saveUserData", "auto_sav.json");

        // once done display maingal screen
        CI.run("closeDialog", "[runtime/player/empireSmallPreffix]txtdlg");
    }
}

var t = new java.lang.Thread(v);
t.start();
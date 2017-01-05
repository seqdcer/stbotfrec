
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
            CI.run("debugPrint [static/stats/empires/list/count]");
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
    
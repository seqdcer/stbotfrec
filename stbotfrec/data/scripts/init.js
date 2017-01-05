//
// This script runs at the start of the program.
//

// set default values in case settings weren't loaded properly
if (CI.run("getVariable settings/audioSubFolder") === null)
    CI.run("setVariable settings/audioSubFolder audio");

if (CI.run("getVariable settings/imagesSubFolder") === null)
    CI.run("setVariable settings/imagesSubFolder images");

if (CI.run("getVariable settings/guiSubFolder") === null)
    CI.run("setVariable settings/guiSubFolder gui");

if (CI.run("getVariable settings/fontSubFolder") === null)
    CI.run("setVariable settings/fontSubFolder gui\\fonts");

if (CI.run("getVariable settings/staticSubFolder") === null)
    CI.run("setVariable settings/staticSubFolder static");

if (CI.run("getVariable settings/screenResolution/width") === null)
    CI.run("setVariable settings/screenResolution/width 800");

if (CI.run("getVariable settings/screenResolution/height") === null)
    CI.run("setVariable settings/screenResolution/height 600");

if (CI.run("getVariable settings/sfxVolume") === null)
    CI.run("setVariable settings/sfxVolume -8");

if (CI.run("getVariable settings/voiceVolume") === null)
    CI.run("setVariable settings/voiceVolume -4");

// default keybinds
if (CI.run("getVariable settings/mapScrollLeft") === null)
    CI.run("setVariable settings/mapScrollLeft LEFT");

if (CI.run("getVariable settings/mapScrollRight") === null)
    CI.run("setVariable settings/mapScrollRight RIGHT");

if (CI.run("getVariable settings/mapScrollUp") === null)
    CI.run("setVariable settings/mapScrollUp UP");

if (CI.run("getVariable settings/mapScrollDown") === null)
    CI.run("setVariable settings/mapScrollDown DOWN");

// set these settings up as enums (meaning they have a limited list of valid values) 
CI.run("setupEnum settings/tacticalCombat static/text/lexicon/options/settings/tacticalCombat");
CI.run("setupEnum settings/tacticalTimer static/text/lexicon/options/settings/tacticalTimer");
CI.run("setupEnum settings/strategicTimer static/text/lexicon/options/settings/strategicTimer");
CI.run("setupEnum settings/startingLevels/0 static/text/lexicon/options/settings/startingLevels");
CI.run("setupEnum settings/startingLevels/1 static/text/lexicon/options/settings/startingLevels");
CI.run("setupEnum settings/startingLevels/2 static/text/lexicon/options/settings/startingLevels");
CI.run("setupEnum settings/startingLevels/3 static/text/lexicon/options/settings/startingLevels");
CI.run("setupEnum settings/startingLevels/4 static/text/lexicon/options/settings/startingLevels");
CI.run("setupEnum settings/victoryCondition static/text/lexicon/options/settings/victoryCondition");
CI.run("setupEnum settings/difficulty static/text/lexicon/options/settings/difficulty");
CI.run("setupEnum settings/minorRaces static/text/lexicon/options/settings/minorRaces");
CI.run("setupEnum settings/galaxySize static/text/lexicon/options/settings/galaxySize");
CI.run("setupEnum settings/galaxyShape static/text/lexicon/options/settings/galaxyShape");
CI.run("setupEnum settings/randomEvents static/text/lexicon/options/settings/randomEvents");

// clear player empire variable
CI.run("setVariable runtime/gamesetup/playerEmpire -1")

CI.run("showScreen opening");
        
      
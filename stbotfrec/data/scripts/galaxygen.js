// used to generate the galaxy
// called by the newsingle script

// width, height
var gwidth = CI.run("getVariable static/gen/galaxy/size/[settings/galaxySize/index]/width");
var gheight = CI.run("getVariable static/gen/galaxy/size/[settings/galaxySize/index]/height");

// get galaxy type
var gshape = CI.run("getVariable settings/galaxyShape/index");

// get gal density and anomaly ratio
var gdensity = CI.run("getVariable static/gen/galaxy/galaxyDensity/" + gshape);
var ganomalyratio = CI.run("getVariable", "static/gen/galaxy/anomalyRatio/" + gshape);

// create object lists
CI.run("createList", "user/galaxy/systems");
CI.run("createList", "user/galaxy/anomalies");


var rngVar, index, systemsCount = 0, anomalyCount = 0;
var systemsList = [];
var systemNames = CI.run("getVariable", "static/gen/galaxy/starNames");
var anomalyNames = [];

// set width, height
CI.run("setVariable", "user/galaxy/mapWidth", gwidth);
CI.run("setVariable", "user/galaxy/mapHeight", gheight);

// generate star system and anomaly map placeholders
CI.run("createList", "user/galaxy/map");

for (var w = 0; w < gwidth; w++)
{
    CI.run("createList", "user/galaxy/map/" + w);
    
    if (w === 0 || w === gwidth - 1) continue;
    
    for (var h = 1; h < gheight - 1; h++)
    {        
        rngVar = CI.run("getRandomDouble");
        
        if (rngVar < gdensity)
        {
            // anomaly or system (indexes start at 1), the first element of either list is thus null/empty
            // the reson for this is that setting system or anomaly in map to 0 means nothing is there so 0 ca't be an object index
            rngVar = CI.run("getRandomDouble");
            
            if (rngVar < ganomalyratio)
            {
                index = anomalyCount + 1;

                CI.run("setVariable", "user/galaxy/map/" + w + "/" + h + "/anomaly", index);
                
                CI.run("setVariable", "user/galaxy/anomalies/" + index + "/x", w);
                CI.run("setVariable", "user/galaxy/anomalies/" + index + "/y", h);
                
                // set random offset within sector
                rngVar = CI.run("getRandomDouble");
                CI.run("setVariable", "user/galaxy/anomalies/" + index + "/offsetX", rngVar);
                rngVar = CI.run("getRandomDouble");
                CI.run("setVariable", "user/galaxy/anomalies/" + index + "/offsetY", rngVar);
                
                anomalyCount++;
            }
            else
            {
                index = systemsCount + 1;
                
                CI.run("setVariable", "user/galaxy/map/" + w + "/" + h + "/system", index);
                
                CI.run("setVariable", "user/galaxy/systems/" + index + "/x", w);
                CI.run("setVariable", "user/galaxy/systems/" + index + "/y", h);
                
                // set random offset within sector
                rngVar = CI.run("getRandomDouble");
                CI.run("setVariable", "user/galaxy/systems/" + index + "/offsetX", rngVar);
                rngVar = CI.run("getRandomDouble");
                CI.run("setVariable", "user/galaxy/systems/" + index + "/offsetY", rngVar);
                
                systemsList[systemsCount] = index;
                systemsCount++;
            }
        }
    }
}

// place races
var empireCount = CI.run("getVariable", "static/stats/empires/list/count");
var racesCount = CI.run("getVariable", "user/races/count");
var namesList = [];

for (rIndex = 0; rIndex < racesCount; rIndex++)
{
    var index = placeRace(rIndex, systemsList, (rIndex < empireCount), empireCount - 1);
}

for (sys = 0; sys < systemsList.length && systemNames.size() > 0; sys++)
{
    if (CI.run("getVariable", "user/galaxy/systems/" + systemsList[sys] + "/inhabited") <= 0)
    {
        placeRandomSystem(systemsList[sys], systemNames);
    }
}

for (any = 1; any <= anomalyCount; any++)
{
    if (CI.run("getVariable", "user/galaxy/anomalies/" + any + "/name") <= 0)
    {
         placeRandomAnomaly(any, anomalyNames);
    }
}

function placeRace(rIndex, systemList, isEmpire, maxEmpireId)
{
    var minDist = (isEmpire) ? CI.run("getVariable", "static/gen/galaxy/minDesiredEmpireDistance/[settings/galaxySize/index]") : 0;
    var itemIndex;
    var x, y;
    
    systemList = systemList.slice(0);
    
    for (var reqDist = minDist; reqDist >= 0; reqDist--)
    {
        itemIndex = CI.run("getRandomLong", systemList.length);
        
        for (var sysCandidate = itemIndex; sysCandidate < systemList.length; sysCandidate++)
        {
            if (isEmpire)
            {
                x = CI.run("getVariable", "user/galaxy/systems/" + systemList[sysCandidate] + "/x");
                y = CI.run("getVariable", "user/galaxy/systems/" + systemList[sysCandidate] + "/y");

                if (!canPlaceEmpire(x, y, reqDist, maxEmpireId))
                {
                    continue;
                }
                else
                {
                    placeHomeSystem(rIndex, systemList[sysCandidate]);
                    return sysCandidate;
                }
            }
        }
        
        for (var sysCandidate = 0; sysCandidate < itemIndex; sysCandidate++)
        {
            if (isEmpire)
            {
                x = CI.run("getVariable", "user/galaxy/systems/" + systemList[sysCandidate] + "/x");
                y = CI.run("getVariable", "user/galaxy/systems/" + systemList[sysCandidate] + "/y");

                if (!canPlaceEmpire(x, y, reqDist, maxEmpireId))
                {
                    continue;
                }
                else
                {
                    placeHomeSystem(rIndex, systemList[sysCandidate]);
                    return sysCandidate;
                }
            }
        }
    }
    
    return -1;
}

function canPlaceEmpire(x, y, reqDist, maxEmpireId)
{
    var index;
    
    for (var xi = x - reqDist; xi <= x + reqDist; xi++)
    {
        for (var yi = y - reqDist; yi <= y + reqDist; yi++)
        {
            index = CI.run("getVariable", "user/galaxy/map/" + xi + "/" + yi + "/system");
            
            if (index > 0)
            {
                var owner = CI.run("getVariable", "user/galaxy/systems/" + index + "/owner");
                
                if (owner && Number(owner) >= 0 && Number(owner) <= maxEmpireId)
                {
                    return false;
                }
            }
        }
    }
    
    return true;
}

function placeHomeSystem(rindex, system)
{
    // get race id
    var raceID = CI.run("getVariable", "user/races/" + rindex + "/id");
    var startingLevel = CI.run("getVariable", "settings/startingLevels/" + rindex + "/index");
            
    // place home system
    CI.run("setVariable", "user/galaxy/systems/" + system + "/inhabited", true);
    CI.run("setVariable", "user/galaxy/systems/" + system + "/homeSystem", true);
    CI.run("setVariable", "user/galaxy/systems/" + system + "/owner", rindex);
    CI.run("setVariable", "user/galaxy/systems/" + system + "/race", raceID);
    CI.run("setVariable", "user/galaxy/systems/" + system + "/pop", "[static/stats/races/" + raceID + "/homeSystem/startingPopulation/" + startingLevel + "]");
    CI.run("setVariable", "user/galaxy/systems/" + system + "/starType", "[static/stats/races/" + raceID + "/homeSystem/starType]");
    CI.run("setVariable", "user/galaxy/systems/" + system + "/hasDilithium", "[static/stats/races/" + raceID + "/homeSystem/hasDilithium]");
    CI.run("setVariable", "user/galaxy/systems/" + system + "/name", "[static/stats/races/" + raceID + "/homeSystem/name]");
    
    // place planets
    CI.run("createList", "user/galaxy/systems/" + system + "/planets");
    var numPlanets = CI.run("getVariable", "static/stats/races/" + raceID + "/homeSystem/numPlanets");
    var planetTypeChances = CI.run("getVariable", "static/gen/galaxy/star/[user/galaxy/systems/" + system + "/starType]/planetTypeChance");
    var planetName;
    var maxPop = [0, 0], pop, level;
    var renamePlanets = false;
    
    for (var i = 0; i < numPlanets; i++)
    {
        planetName = CI.run("getVariable", "static/stats/races/" + raceID + "/homeSystem/planets/" + i + "/name");
        
        if(planetName === null)
        {   // add random planet
            var popChange = addRandomPlanet(system, planetTypeChances);
            
            maxPop[0] += popChange[0];
            maxPop[1] += popChange[1];
            
            renamePlanets = true;
        }
        else
        {   // add planet
            CI.run("setVariable", "user/galaxy/systems/" + system + "/planets/" + i + "/name", planetName);
            CI.run("setVariable", "user/galaxy/systems/" + system + "/planets/" + i + "/type", "[static/stats/races/" + raceID + "/homeSystem/planets/" + i + "/type]");
            CI.run("setVariable", "user/galaxy/systems/" + system + "/planets/" + i + "/size", "[static/stats/races/" + raceID + "/homeSystem/planets/" + i + "/size]");
            CI.run("setVariable", "user/galaxy/systems/" + system + "/planets/" + i + "/img", "[static/stats/races/" + raceID + "/homeSystem/planets/" + i + "/img]");
            CI.run("setVariable", "user/galaxy/systems/" + system + "/planets/" + i + "/bonusPerc", "[static/stats/races/" + raceID + "/homeSystem/planets/" + i + "/bonusPerc]");
            CI.run("setVariable", "user/galaxy/systems/" + system + "/planets/" + i + "/bonusType", "[static/stats/races/" + raceID + "/homeSystem/planets/" + i + "/bonusType]");
             
            level = CI.run("getVariable", "static/stats/races/" + raceID + "/homeSystem/planets/" + i + "/reqStartLevel");
            pop = CI.run("getVariable", "static/stats/races/" + raceID + "/homeSystem/planets/" + i + "/maxPop");
            
            maxPop[1] += pop;
            
            CI.run("setVariable", "user/galaxy/systems/" + system + "/planets/" + i + "/maxPop", pop);
            
            if (level <= startingLevel)
            {   // start terraformed
                maxPop[0] += pop;
            }
            else
            {   // add terraforming cost
                pop = pop * CI.run("getVariable", "static/gen/galaxy/planet/[static/stats/races/" + raceID + "/homeSystem/planets/" + i + "/type]/terraformCostMult");
                CI.run("setVariable", "user/galaxy/systems/" + system + "/planets/" + i + "/terraformCost", pop);
                
                if (pop == 0)
                {   // no cost after all, start terraformed
                    maxPop[0] += pop;
                }
            }
        }
    }
    
    CI.run("setVariable", "user/galaxy/systems/" + system + "/maxPop", maxPop[0]);
    CI.run("setVariable", "user/galaxy/systems/" + system + "/maxPopCapacity", maxPop[1]);
    
    if (renamePlanets)
    {
        for (var i = 0; i < numPlanets; i++)
        {
            CI.run("setVariable", "user/galaxy/systems/" + system + "/planets/" + i + "/name", "[user/galaxy/systems/" + system + "/name] " + (i + 1));
        }
    }
    
    // add system to list of owned system for this race
    CI.run("addToList", "user/races/" + rindex + "/systems", system);
    CI.run("setVariable", "user/races/" + rindex + "/home", system);
}

function placeRandomSystem(system, systemNames)
{
    // place system
    var rng = CI.run("getRandomDouble");
    var starTypes = CI.run("getVariable", "static/gen/galaxy/star/count");
    var star = 0;
    var variable;
    
    for (; star < starTypes; star++)
    {
        variable = CI.run("getVariable", "static/gen/galaxy/star/" + star + "/occurance");
        
        if (rng < variable)
        {
            break;
        }
        else
        {
            rng -= variable;
        }
    }
    
    if (star >= starTypes) star = starTypes - 1;
    
    CI.run("setVariable", "user/galaxy/systems/" + system + "/starType", star);
    CI.run("setVariable", "user/galaxy/systems/" + system + "/hasDilithium", (CI.run("getRandomDouble") < CI.run("getVariable", "static/gen/galaxy/star/" + star + "/dilChance")) ? true : false);
    
    
    variable = systemNames.get(CI.run("getRandomLong", systemNames.length));
    systemNames.remove(variable);
    
    CI.run("setVariable", "user/galaxy/systems/" + system + "/name", variable);
    
    // place planets
    CI.run("createList", "user/galaxy/systems/" + system + "/planets");
    var numPlanets = CI.run("getRandomLong", 1, "[static/gen/galaxy/maxPlanetsPerSystem]");
    var planetTypeChances = CI.run("getVariable", "static/gen/galaxy/star/[user/galaxy/systems/" + system + "/starType]/planetTypeChance");
    var maxPop = [0, 0];
    
    // CI.run("debugPrint", variable + ": " + numPlanets)
    
    for (var i = 0; i < numPlanets; i++)
    {
        // add random planet
        var popChange = addRandomPlanet(system, planetTypeChances);

        maxPop[0] += popChange[0];
        maxPop[1] += popChange[1];
    }
    
    CI.run("setVariable", "user/galaxy/systems/" + system + "/pop", 0);
    CI.run("setVariable", "user/galaxy/systems/" + system + "/maxPop", maxPop[0]);
    CI.run("setVariable", "user/galaxy/systems/" + system + "/maxPopCapacity", maxPop[1]);
    
    for (var i = 0; i < numPlanets; i++)
    {
        CI.run("setVariable", "user/galaxy/systems/" + system + "/planets/" + i + "/name", variable + " " + (i + 1));
    }
}

function addRandomPlanet(system, planetTypeChances)
{
    var rng = CI.run("getRandomDouble");
    var planetType = 0;
    
    for (; planetType < planetTypeChances.size(); planetType++)
    {
        if (rng < planetTypeChances.get(planetType))
        {
            break;
        }
        else
        {
            rng -= planetTypeChances.get(planetType);
        }
    }

    if (planetType >= planetTypeChances.size())
    {
        planetType = planetTypeChances.size() - 1;
        
        if (planetType < 0) planetType = 0;
    }
    
    var planetCount = CI.run("getVariable", "user/galaxy/systems/" + system + "/planets/count");
    var otherPlanetType;
    var pIndex = planetCount;
    var otherSortPos, mySortPos = CI.run("getRandomFromList", "static/gen/galaxy/planet/" + planetType + "/sortPos");
    var planetSize = CI.run("getRandomLong", "[static/gen/galaxy/planet/" + planetType + "/maxPop/count]");
    
    for (var i = planetCount - 1; i >= 0; i--)
    {
        otherPlanetType = CI.run("getVariable",  "user/galaxy/systems/" + system + "/planets/" + i + "/type");
        
        otherSortPos = CI.run("getRandomFromList", "static/gen/galaxy/planet/" + otherPlanetType + "/sortPos");
        
        if (otherSortPos < mySortPos || (otherSortPos == mySortPos && CI.run("getRandomDouble") < 0.5))
        {
            break;
        }
        else
        {
            pIndex = i;
        }
    }
    
    // make room for new planet (potentionaly push others further down the list)
    CI.run("addToList", "user/galaxy/systems/" + system + "/planets/", pIndex, null);
    
    // setup planet
    CI.run("setVariable", "user/galaxy/systems/" + system + "/planets/" + pIndex + "/type", planetType);
    CI.run("setVariable", "user/galaxy/systems/" + system + "/planets/" + pIndex + "/size", planetSize);
    CI.run("setVariable", "user/galaxy/systems/" + system + "/planets/" + pIndex + "/img",
        CI.run("getRandomFromList", "static/gen/galaxy/planet/" + planetType + "/img/" + planetSize));
    CI.run("setVariable", "user/galaxy/systems/" + system + "/planets/" + pIndex + "/bonusPerc",
        CI.run("getRandomFromList", "static/gen/galaxy/planet/" + planetType + "/bonusPerc/" + planetSize));
    CI.run("setVariable", "user/galaxy/systems/" + system + "/planets/" + pIndex + "/bonusType", "[static/gen/galaxy/planet/" + planetType + "/bonusType]");

    pop = CI.run("getRandomFromList", "static/gen/galaxy/planet/" + planetType + "/maxPop/" + planetSize);

    CI.run("setVariable", "user/galaxy/systems/" + system + "/planets/" + pIndex + "/maxPop", pop);

    var terraformCost = pop * CI.run("getVariable", "static/gen/galaxy/planet/" + planetType + "/terraformCostMult");
    
    if (terraformCost <= 0)
    {
        return [pop, pop];
    }
    else
    {   // add terraforming cost
        CI.run("setVariable", "user/galaxy/systems/" + system + "/planets/" + pIndex + "/terraformCost", terraformCost);

        return [0, pop];
    }
}

function placeRandomAnomaly(anomaly, anomalyNames)
{
    // place anomaly
    var rng = CI.run("getRandomDouble");
    var anomalyTypes = CI.run("getVariable", "static/gen/galaxy/anomaly/count");
    var anomalyType = 0;
    var variable;
    
    for (; anomalyType < anomalyTypes; anomalyType++)
    {
        variable = CI.run("getVariable", "static/gen/galaxy/anomaly/" + anomalyType + "/occurance");
        
        if (rng < variable)
        {
            break;
        }
        else
        {
            rng -= variable;
        }
    }
    
    if (anomalyType >= anomalyTypes) anomalyType = anomalyTypes - 1;
    
    var name =  CI.run("getVariable", "user/galaxy/anomalies/" + anomaly + "/name");
    
    name = placeAnomaly(anomalyType, anomaly, anomalyNames)

    if (name === null)
    {
        placeRandomAnomaly(anomaly, anomalyNames);
    }
}

function placeAnomaly(anomalyType, anomaly, anomalyNames)
{
    // if the anomaly causes damage, make sure no race starting system is within it's range
    var dmg = CI.run("getVariable", "static/gen/galaxy/anomaly/" + anomalyType + "/maxDamage");
    var xa = CI.run("getVariable", "user/galaxy/anomalies/" + anomaly + "/x");
    var ya = CI.run("getVariable", "user/galaxy/anomalies/" + anomaly + "/y");
        
    if (dmg > 0)
    {   // get range
        var dmgRange = CI.run("getVariable", "static/gen/galaxy/anomaly/" + anomalyType + "/effectRange");

        for (var x = x - dmgRange; x < xa + dmgRange; x++)
        {
            for (var y = y - dmgRange; y < ya + dmgRange; y++)
            {
                if (CI.run("getVariable", "user/galaxy/systems/[user/galaxy/map/" + x + "/" + y + "/system]/inhabited") > 0)
                {
                    return null;
                }
            }
        }
    }
    
    // setup
    CI.run("setVariable", "user/galaxy/anomalies/" + anomaly + "/anomalyType", anomalyType);
    
    // if it's a worm hole, try to find a counterpart to form a stable connection
    if (CI.run("getVariable", "static/gen/galaxy/anomaly/" + anomalyType + "/shipDisplacement") > 0)
    {
        var linkChance = CI.run("getVariable", "static/gen/galaxy/anomaly/" + anomalyType + "/linkChance");
        
        for (var anomID = 1; anomID < anomaly; anomID++)
        {
            if (CI.run("getVariable", "user/galaxy/anomalies/" + anomID + "/anomalyType") == anomalyType &&
                CI.run("getVariable", "user/galaxy/anomalies/" + anomID + "/isLinked") <= 0 &&
                CI.run("getRandomDouble") <  linkChance)
            {
                CI.run("setVariable", "user/galaxy/anomalies/" + anomaly + "/isLinked", true);
                CI.run("setVariable", "user/galaxy/anomalies/" + anomaly + "/destX", "[user/galaxy/anomalies/" + anomID + "/x]");
                CI.run("setVariable", "user/galaxy/anomalies/" + anomaly + "/destY", "[user/galaxy/anomalies/" + anomID + "/y]");
                CI.run("setVariable", "user/galaxy/anomalies/" + anomID + "/isLinked", true);
                CI.run("setVariable", "user/galaxy/anomalies/" + anomID + "/destX", "[user/galaxy/anomalies/" + anomaly + "/x]");
                CI.run("setVariable", "user/galaxy/anomalies/" + anomID + "/destY", "[user/galaxy/anomalies/" + anomaly + "/y]");

                break;
            }
        }
        
        // if no stable link, select random destination
        if (CI.run("getVariable", "user/galaxy/anomalies/" + anomaly + "/isLinked") <= 0)
        {
            while (true)
            {
                var x = CI.run("getRandomLong", "[user/galaxy/map/count]");
                var y = CI.run("getRandomLong", "[user/galaxy/map/0/count]");
                
                // check to make sure the object (if any) we linked to is not of the same anomaly type
                var anomID = CI.run("getVariable", "user/galaxy/map/" + x + "/" + y + "/anomaly");
                
                if (anomID <= 0 || CI.run("getVariable", "user/galaxy/anomalies/" + anomID + "/anomalyType") !== anomalyType)
                {
                    CI.run("setVariable", "user/galaxy/anomalies/" + anomaly + "/destX", x);
                    CI.run("setVariable", "user/galaxy/anomalies/" + anomaly + "/destY", y);
                    break;
                }
            }
        }
    }
    
    // setup random pattern state if appropriate
    if (CI.run("getVariable", "static/gen/galaxy/anomaly/" + anomalyType + "/effectPattern") > 0)
    {
        CI.run("setVariable", "user/galaxy/anomalies/" + anomaly + "/patternState", CI.run("getRandomLong", 100));
    }
    
    // setup name
    do
    {
        var anomalyName = CI.run("getRandomFromList", "static/gen/galaxy/anomalyNames/" + anomalyType);

        anomalyName = anomalyName.replace("%s1", xa);
        anomalyName = anomalyName.replace("%s2", ya);

        anomalyName = anomalyName.replace("%s", xa);
        anomalyName = anomalyName.replace("%s", ya);
        
        if (anomalyNames[anomalyName] !== true)
        {
            anomalyNames[anomalyName] = true;
            CI.run("setVariable", "user/galaxy/anomalies/" + anomaly + "/name", anomalyName);
            
            return anomalyName;
        }
    }
    while (true);
}
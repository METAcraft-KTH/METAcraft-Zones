# METAcraft Zones

Zone library for METAcraft mods.

#### For admins:

Add a zone (blocks nether portals by default, but nothing else):  
`/zone create <name> [region <x> <z> <x> <z> | box <x> <y> <z> <x> <y> <z> | circle <x> <y> <z> <radius> | sphere <x> <y> <z> <radius> | biome <biome|tag> [alwaysCheckSourceDim]]`

Extend a zone to cover another area:
`/zone extend <name> [region <x> <z> <x> <z> | box <x> <y> <z> <x> <y> <z> | circle <x> <y> <z> <radius> | sphere <x> <y> <z> <radius> | biome <biome|tag> [alwaysCheckSourceDim]]`

Shrink a zone to only cover the shared area between the old and new area.
`/zone restrict-to <name> [region <x> <z> <x> <z> | box <x> <y> <z> <x> <y> <z> | circle <x> <y> <z> <radius> | sphere <x> <y> <z> <radius> | biome <biome|tag> [alwaysCheckSourceDim]]`

Invert a zone area, making it cover everything outside the given area:
`/zone negate <name>`

Make a zone apply in another dimension (with respect to coordinate scale of course):
`/zone dimension add <dim>`
If a biome zone has alwaysCheckSourceDim set to true, it will query the overworld for biome checks, if false it will query the target dimension (false is default).

Remove a zone from another dimension:
`/zone dimension remove <dim>`

List all zones: `/zone list`

Find info about a zone: `/zone get <name>`

Change zone priority (in case multiple zones overlap, by default smaller zones have higher priority):  
`/zone set-priority <name> <priority>`

Remove a zone: `/zone remove <name>`

All commands have permission level 2 and the permission node `se.datasektionen.mc.zones.admin`

#### For developers
To add data to a zone, register a new ZoneDataType to `ZoneDataRegistry::REGISTRY`.
You can access the zones using `ZoneManager::getInstance` to fetch and modify the data.
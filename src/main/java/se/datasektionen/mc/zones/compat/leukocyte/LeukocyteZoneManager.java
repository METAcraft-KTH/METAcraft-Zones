package se.datasektionen.mc.zones.compat.leukocyte;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import se.datasektionen.mc.zones.METAcraftZones;
import se.datasektionen.mc.zones.ZoneManager;
import se.datasektionen.mc.zones.compat.mixin.AccessorIndexedAuthorityMap;
import se.datasektionen.mc.zones.zone.Zone;
import xyz.nucleoid.leukocyte.Leukocyte;
import xyz.nucleoid.leukocyte.authority.Authority;

import java.util.ArrayList;
import java.util.List;

public class LeukocyteZoneManager {

	private static final String SEPARATOR = "-+-";

	public static String getAuthorityName(Zone zone) {
		return zone.getName() + SEPARATOR + METAcraftZones.MODID;
	}

	public static Authority getAuthorityFromZone(Zone zone) {
		return Leukocyte.get(zone.getWorld().getServer()).getAuthorityByKey(getAuthorityName(zone));
	}

	public static boolean isMETAcraftZoneName(String name) {
		return name.endsWith(SEPARATOR + METAcraftZones.MODID);
	}

	public static String getZoneNameFromAuthorityName(String name) {
		return name.substring(0, name.length() - SEPARATOR.length() - METAcraftZones.MODID.length());
	}

	public static void onZoneAdd(MinecraftServer server, Zone zone) {
		String name = getAuthorityName(zone);
		Leukocyte.get(server).addAuthority(Authority.create(name).addShape(
				name, new ZoneShape(zone.getName())
		));
	}

	public static void onZoneRemove(MinecraftServer server, Zone zone) {
		Leukocyte.get(server).removeAuthority(getAuthorityName(zone));
	}

	public static void updateZoneDimensions(MinecraftServer server, Zone zone) {
		var leukocyte = Leukocyte.get(server);
		if (leukocyte.getAuthorities() instanceof AccessorIndexedAuthorityMap map) {
			var name = getAuthorityName(zone);
			map.callRemoveFromDimension(name);
			map.callAddToDimension(leukocyte.getAuthorityByKey(name));
		}
	}

	public static void init() {
		ZoneShape.REGISTRY.register(METAcraftZones.MODID, ZoneShape.CODEC);
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			var zones = ZoneManager.getInstance(server);
			var leukocyte = Leukocyte.get(server);


			List<String> authoritiesToRemove = new ArrayList<>();
			leukocyte.getAuthorities().forEach(authority -> {
				if (isMETAcraftZoneName(authority.getKey()) && !zones.containsZone(getZoneNameFromAuthorityName(authority.getKey()))) {
					authoritiesToRemove.add(authority.getKey());
				}
			});
			for (String authority : authoritiesToRemove) {
				METAcraftZones.LOGGER.error("Found METAcraft authority " + authority + " with no matching zone, removing it.");
				leukocyte.removeAuthority(authority);
			}

			zones.getZones().getZones().forEach(zone -> {
				String authorityName = getAuthorityName(zone);
				Authority authority = leukocyte.getAuthorityByKey(authorityName);
				if (authority == null) {
					METAcraftZones.LOGGER.warn("METAcraft zone " + zone.getName() + " has no authority! Creating it.");
					onZoneAdd(server, zone);
				}
			});

		});
	}

}

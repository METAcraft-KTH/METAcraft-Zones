package se.datasektionen.mc.zones.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.datasektionen.mc.zones.EntityData;
import se.datasektionen.mc.zones.ZoneManager;
import se.datasektionen.mc.zones.zone.Zone;

@Mixin(Entity.class)
public abstract class MixinEntity implements EntityData {
	@Shadow public abstract BlockPos getBlockPos();

	@Shadow @Nullable public abstract MinecraftServer getServer();

	@Shadow private World world;
	@Shadow public int age;
	@Unique
	private Zone currentZone;

	@Override
	public Zone METAcraft_Zones$getCurrentZone() {
		return currentZone;
	}

	@Inject(
			method = "tick",
			at = @At("HEAD")
	)
	public void tick(CallbackInfo ci) {
		if (!world.isClient() && this.age % 100 == 0) {
			if (currentZone != null && !currentZone.contains(this.getBlockPos())) {
				currentZone.removeFromZone((Entity) (Object) this);
				currentZone = null;
			}
			for (Zone zone : ZoneManager.getInstance(getServer()).getZones().getZones(this.world.getRegistryKey())) {
				if (zone == currentZone) {
					break; //Skips zones with lower priority than the one we are in.
				}
				if (zone.contains(this.getBlockPos())) {
					if (currentZone != null) {
						currentZone.removeFromZone((Entity) (Object) this);
					}
					currentZone = zone;
					currentZone.addToZone((Entity) (Object) this);
					break;
				}
			}
		}
	}

}

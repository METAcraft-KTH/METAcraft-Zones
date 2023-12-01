package se.datasektionen.mc.zones.zone.data;

import com.mojang.serialization.Codec;
import net.minecraft.text.Text;
import se.datasektionen.mc.zones.zone.Zone;

import java.util.Timer;

public abstract class ZoneData {

	protected static final Timer TIMER = new Timer(true);

	protected transient Zone zone;

	public final void setZone(Zone zone) {
		this.zone = zone;
	}

	public static final Codec<ZoneData> REGISTRY_CODEC = ZoneDataRegistry.REGISTRY.getCodec().dispatch(
			ZoneData::getType, ZoneDataType::codec
	);

	public void markDirty() {
		zone.markDirty();
	}

	public abstract ZoneDataType<? extends ZoneData> getType();

	public Text toText() {
		return Text.literal(this.toString());
	}

}

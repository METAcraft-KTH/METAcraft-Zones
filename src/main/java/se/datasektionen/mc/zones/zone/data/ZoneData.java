package se.datasektionen.mc.zones.zone.data;

import com.mojang.serialization.Codec;

public interface ZoneData {

	Codec<ZoneData> REGISTRY_CODEC = ZoneDataRegistry.REGISTRY.getCodec().dispatch(
			ZoneData::getType, ZoneDataType::codec
	);

	ZoneDataType<? extends ZoneData> getType();

}

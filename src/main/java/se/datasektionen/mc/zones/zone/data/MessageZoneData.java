package se.datasektionen.mc.zones.zone.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.dynamic.Codecs;

import java.util.Optional;

public class MessageZoneData extends ZoneDataEntityTracking {

	public static final Codec<MessageZoneData> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Codecs.TEXT.optionalFieldOf("enterMessage").forGetter(data -> data.enterMessage),
					Codecs.TEXT.optionalFieldOf("leaveMessage").forGetter(data -> data.leaveMessage)
			).apply(instance, MessageZoneData::new)
	);

	protected Optional<Text> enterMessage;
	protected Optional<Text> leaveMessage;

	public MessageZoneData(Optional<Text> enterMessage, Optional<Text> leaveMessage) {
		this.enterMessage = enterMessage;
		this.leaveMessage = leaveMessage;
	}

	@Override
	public void onEnter(Entity entity) {
		if (entity instanceof PlayerEntity player) {
			enterMessage.ifPresent(msg -> {
				player.sendMessage(msg, false);
			});
		}
	}

	@Override
	public void onLeave(Entity entity) {
		if (entity instanceof PlayerEntity player) {
			leaveMessage.ifPresent(msg -> {
				player.sendMessage(msg, false);
			});
		}
	}

	public void setEnterMessage(Optional<Text> text) {
		enterMessage = text;
		markDirty();
	}

	public void setLeaveMessage(Optional<Text> text) {
		leaveMessage = text;
		markDirty();
	}

	public Optional<Text> getEnterMessage() {
		return enterMessage;
	}

	public Optional<Text> getLeaveMessage() {
		return leaveMessage;
	}

	@Override
	public ZoneDataType<? extends ZoneData> getType() {
		return ZoneDataRegistry.MESSAGE;
	}

	@Override
	public Text toText() {
		MutableText text = Text.literal("MessageZoneData[");
		enterMessage.ifPresent(msg -> {
			text.append(Text.literal("enterMessage=").append(msg).append(","));
		});
		leaveMessage.ifPresent(msg -> {
			text.append(Text.literal("exitMessage=").append(msg).append(","));
		});
		return text;
	}
}

package se.datasektionen.mc.zones.zone.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.dynamic.Codecs;
import se.datasektionen.mc.zones.EntityData;

import java.util.*;

public class MessageZoneData extends ZoneDataEntityTracking {

	public static final Codec<MessageZoneData> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Codecs.TEXT.optionalFieldOf("enterMessage").forGetter(data -> data.enterMessage),
					Codecs.TEXT.optionalFieldOf("leaveMessage").forGetter(data -> data.leaveMessage),
					Codec.BOOL.fieldOf("showLeaveMessageWhenEnteringOtherZone").orElse(true).forGetter(data -> data.showLeaveMessageWhenEnteringOtherZone)
			).apply(instance, MessageZoneData::new)
	);

	protected Optional<Text> enterMessage;
	protected Optional<Text> leaveMessage;
	protected boolean showLeaveMessageWhenEnteringOtherZone;

	public MessageZoneData(Optional<Text> enterMessage, Optional<Text> leaveMessage, boolean showLeaveMessageWhenEnteringOtherZone) {
		this.enterMessage = enterMessage;
		this.leaveMessage = leaveMessage;
		this.showLeaveMessageWhenEnteringOtherZone = showLeaveMessageWhenEnteringOtherZone;
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
			TIMER.schedule(new TimerTask() {
				@Override
				public void run() {
					zone.getWorld().getServer().execute(() -> {
						if (!showLeaveMessageWhenEnteringOtherZone && ((EntityData) entity).METAcraft_Zones$getCurrentZone() != null) {
							return;
						}
						leaveMessage.ifPresent(msg -> {
							player.sendMessage(msg, false);
						});
					});
				}
			}, 50);
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

	public void setShowLeaveMessageWhenEnteringOtherZone(boolean showLeaveMessageWhenEnteringOtherZone) {
		this.showLeaveMessageWhenEnteringOtherZone = showLeaveMessageWhenEnteringOtherZone;
		markDirty();
	}

	public Optional<Text> getEnterMessage() {
		return enterMessage;
	}

	public Optional<Text> getLeaveMessage() {
		return leaveMessage;
	}

	public boolean showLeaveMessageWhenEnteringOtherZone() {
		return showLeaveMessageWhenEnteringOtherZone;
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
		return text.append("showLeaveMessageWhenEnteringOtherZone=" + showLeaveMessageWhenEnteringOtherZone + "]");
	}
}

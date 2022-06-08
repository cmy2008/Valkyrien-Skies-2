package org.valkyrienskies.mod.mixin.client.world;

import java.util.function.BooleanSupplier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.game.ships.QueryableShipDataImpl;
import org.valkyrienskies.core.game.ships.ShipObjectClient;
import org.valkyrienskies.core.game.ships.ShipObjectClientWorld;
import org.valkyrienskies.mod.common.IShipObjectWorldClientProvider;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevel implements IShipObjectWorldClientProvider {

    @Unique
    private final ShipObjectClientWorld shipObjectWorld = new ShipObjectClientWorld(new QueryableShipDataImpl<>());

    @NotNull
    @Override
    public ShipObjectClientWorld getShipObjectWorld() {
        return shipObjectWorld;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void postTick(final BooleanSupplier shouldKeepTicking, final CallbackInfo ci) {
        // Tick the ship world
        shipObjectWorld.tickShips();
    }

    @Inject(
        method = "playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onPlaySound(final Player player, final double x, final double y, final double z,
        final SoundEvent sound, final SoundSource category,
        final float volume, final float pitch, final CallbackInfo ci) {
        final ClientLevel self = ClientLevel.class.cast(this);
        final ShipObjectClient shipObject = VSGameUtilsKt.getShipObjectManagingPos(self, (int) x >> 4, (int) z >> 4);
        if (shipObject != null) {
            // in-world position
            final Vector3d p = shipObject.getRenderTransform()
                .getShipToWorldMatrix().transformPosition(new Vector3d(x, y, z));

            playLocalSound(p.x, p.y, p.z, sound, category, volume, pitch, false);
            ci.cancel();
        }
    }

    @Shadow
    public abstract void playLocalSound(double x, double y, double z, SoundEvent sound, SoundSource category,
        float volume,
        float pitch, boolean bl);

}
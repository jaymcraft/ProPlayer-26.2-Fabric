package net.shasankp000.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.shasankp000.GraphicalUserInterface.ThreatDebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to inject threat debug rendering into world rendering
 */
@Mixin(LevelRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "extractLevel", at = @At("TAIL"))
    private void onRenderWorld(DeltaTracker tickCounter, Camera camera, float partialTick, CallbackInfo ci) {
        // Render threat debug overlays in world space
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && camera != null) {
            ThreatDebugRenderer.renderThreatOverlays(camera);
        }
    }
}

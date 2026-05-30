package net.shasankp000.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to inject threat debug rendering into the HUD
 */
@Mixin(Gui.class)
public class InGameHudMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onRenderHud(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        // Rendering moved to WorldRendererMixin for proper 3D world space rendering
        // This mixin is kept for future HUD elements
    }
}

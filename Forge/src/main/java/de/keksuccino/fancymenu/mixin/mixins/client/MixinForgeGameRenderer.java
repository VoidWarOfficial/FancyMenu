package de.keksuccino.fancymenu.mixin.mixins.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.fancymenu.event.acara.EventHandler;
import de.keksuccino.fancymenu.event.events.screen.RenderScreenEvent;
import de.keksuccino.fancymenu.mixin.MixinCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinForgeGameRenderer {

    private PoseStack cachedStack = null;
    private int cachedMouseX = 0;
    private int cachedMouseY = 0;
    private float cachedPartial = 0.0F;

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;drawScreen(Lnet/minecraft/client/gui/screens/Screen;Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V"))
    private boolean wrapRenderScreenFancyMenu(Screen instance, PoseStack poseStack, int mouseX, int mouseY, float partial) {
        this.cachedStack = poseStack;
        this.cachedMouseX = mouseX;
        this.cachedMouseY = mouseY;
        this.cachedPartial = partial;
        MixinCache.currentRenderScreen = instance;
        RenderScreenEvent.Pre e = new RenderScreenEvent.Pre(Minecraft.getInstance().screen, poseStack, mouseX, mouseY, partial);
        EventHandler.INSTANCE.postEvent(e);
        return !e.isCanceled();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;drawScreen(Lnet/minecraft/client/gui/screens/Screen;Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", shift = At.Shift.AFTER))
    private void afterRenderScreenFancyMenu(float $$0, long $$1, boolean $$2, CallbackInfo info) {
        RenderScreenEvent.Post e = new RenderScreenEvent.Post(Minecraft.getInstance().screen, (this.cachedStack != null) ? this.cachedStack : new PoseStack(), this.cachedMouseX, this.cachedMouseY, this.cachedPartial);
        EventHandler.INSTANCE.postEvent(e);
    }

}

package de.keksuccino.fancymenu.customization.layout.editor;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.fancymenu.customization.slideshow.SlideshowHandler;
import de.keksuccino.fancymenu.misc.InputConstants;
import de.keksuccino.fancymenu.rendering.ui.UIBase;
import de.keksuccino.fancymenu.rendering.ui.scroll.scrollarea.ScrollArea;
import de.keksuccino.fancymenu.rendering.ui.scroll.scrollarea.entry.ScrollAreaEntry;
import de.keksuccino.fancymenu.rendering.ui.scroll.scrollarea.entry.TextListScrollAreaEntry;
import de.keksuccino.fancymenu.rendering.ui.scroll.scrollarea.entry.TextScrollAreaEntry;
import de.keksuccino.fancymenu.rendering.ui.tooltip.Tooltip;
import de.keksuccino.fancymenu.rendering.ui.tooltip.TooltipHandler;
import de.keksuccino.fancymenu.rendering.ui.widget.ExtendedButton;
import de.keksuccino.fancymenu.utils.LocalizationUtils;
import de.keksuccino.konkrete.gui.content.AdvancedButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ChoosePanoramaScreen extends Screen {

    protected Consumer<String> callback;
    protected String selectedPanoramaName = null;

    protected ScrollArea panoramaListScrollArea = new ScrollArea(0, 0, 0, 0);
    protected ExtendedButton doneButton;
    protected ExtendedButton cancelButton;

    public ChoosePanoramaScreen(@Nullable String preSelectedPanorama, @NotNull Consumer<String> callback) {

        super(Component.translatable("fancymenu.panorama.choose"));

        this.callback = callback;
        this.updateSlideshowScrollAreaContent();

        if (preSelectedPanorama != null) {
            for (ScrollAreaEntry e : this.panoramaListScrollArea.getEntries()) {
                if ((e instanceof PanoramaScrollEntry a) && a.panorama.equals(preSelectedPanorama)) {
                    a.setSelected(true);
                    this.setSelectedPanorama(a);
                    break;
                }
            }
        }

        this.doneButton = new ExtendedButton(0, 0, 150, 20, Component.translatable("fancymenu.guicomponents.done"), (button) -> {
            this.callback.accept(this.selectedPanoramaName);
        }) {
            @Override
            public void renderWidget(@NotNull PoseStack pose, int mouseX, int mouseY, float partial) {
                if (ChoosePanoramaScreen.this.selectedPanoramaName == null) {
                    TooltipHandler.INSTANCE.addWidgetTooltip(this, Tooltip.create(LocalizationUtils.splitLocalizedLines("fancymenu.panorama.choose.no_panorama_selected")).setDefaultBackgroundColor(), false, true);
                    this.active = false;
                } else {
                    this.active = true;
                }
                super.renderWidget(pose, mouseX, mouseY, partial);
            }
        }.setAutoRegisterToScreen(true);
        UIBase.applyDefaultButtonSkinTo(this.doneButton);

        this.cancelButton = new ExtendedButton(0, 0, 150, 20, Component.translatable("fancymenu.guicomponents.cancel"), (button) -> {
            this.callback.accept(null);
        }).setAutoRegisterToScreen(true);
        UIBase.applyDefaultButtonSkinTo(this.cancelButton);

    }

    @Override
    public void onClose() {
        this.callback.accept(null);
    }

    @Override
    public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partial) {

        RenderSystem.enableBlend();

        fill(pose, 0, 0, this.width, this.height, UIBase.SCREEN_BACKGROUND_COLOR.getRGB());

        Component titleComp = this.title.copy().withStyle(Style.EMPTY.withBold(true));
        this.font.draw(pose, titleComp, 20, 20, -1);

        this.font.draw(pose, Component.translatable("fancymenu.panorama.choose.available_panoramas"), 20, 50, -1);

        this.panoramaListScrollArea.setWidth((this.width / 2) - 40, true);
        this.panoramaListScrollArea.setHeight(this.height - 85, true);
        this.panoramaListScrollArea.setX(20, true);
        this.panoramaListScrollArea.setY(50 + 15, true);
        this.panoramaListScrollArea.render(pose, mouseX, mouseY, partial);

        this.doneButton.setX(this.width - 20 - this.doneButton.getWidth());
        this.doneButton.setY(this.height - 20 - 20);
        this.doneButton.render(pose, mouseX, mouseY, partial);

        this.cancelButton.setX(this.width - 20 - this.cancelButton.getWidth());
        this.cancelButton.setY(this.doneButton.getY() - 5 - 20);
        this.cancelButton.render(pose, mouseX, mouseY, partial);

        super.render(pose, mouseX, mouseY, partial);

    }

    protected void setSelectedPanorama(@Nullable ChoosePanoramaScreen.PanoramaScrollEntry entry) {
        if (entry == null) {
            this.selectedPanoramaName = null;
            return;
        }
        this.selectedPanoramaName = entry.panorama;
    }

    protected void updateSlideshowScrollAreaContent() {
        this.panoramaListScrollArea.clearEntries();
        for (String s : SlideshowHandler.getSlideshowNames()) {
            PanoramaScrollEntry e = new PanoramaScrollEntry(this.panoramaListScrollArea, s, (entry) -> {
                this.setSelectedPanorama((PanoramaScrollEntry)entry);
            });
            this.panoramaListScrollArea.addEntry(e);
        }
        if (this.panoramaListScrollArea.getEntries().isEmpty()) {
            this.panoramaListScrollArea.addEntry(new TextScrollAreaEntry(this.panoramaListScrollArea, Component.translatable("fancymenu.panorama.choose.no_panoramas"), (entry) -> {}));
        }
        int totalWidth = this.panoramaListScrollArea.getTotalEntryWidth();
        for (ScrollAreaEntry e : this.panoramaListScrollArea.getEntries()) {
            e.setWidth(totalWidth);
        }
    }

    @Override
    public boolean keyPressed(int button, int $$1, int $$2) {

        if (button == InputConstants.KEY_ENTER) {
            if (this.selectedPanoramaName != null) {
                this.callback.accept(this.selectedPanoramaName);
                return true;
            }
        }

        return super.keyPressed(button, $$1, $$2);

    }

    public static class PanoramaScrollEntry extends TextListScrollAreaEntry {

        public String panorama;

        public PanoramaScrollEntry(ScrollArea parent, @NotNull String panorama, @NotNull Consumer<TextListScrollAreaEntry> onClick) {
            super(parent, Component.literal(panorama).setStyle(Style.EMPTY.withColor(TEXT_COLOR_GREY_1.getRGB())), LISTING_DOT_BLUE, onClick);
            this.panorama = panorama;
        }

    }

}
package de.keksuccino.fancymenu.customization.element.elements.button.vanillawidget;

import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.fancymenu.customization.widget.WidgetMeta;
import de.keksuccino.fancymenu.customization.element.ElementBuilder;
import de.keksuccino.fancymenu.customization.element.IHideableElement;
import de.keksuccino.fancymenu.customization.element.anchor.ElementAnchorPoints;
import de.keksuccino.fancymenu.customization.element.elements.button.custombutton.ButtonEditorElement;
import de.keksuccino.fancymenu.customization.element.elements.button.custombutton.ButtonElement;
import de.keksuccino.fancymenu.util.rendering.ui.widget.CustomizableWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class VanillaWidgetElement extends ButtonElement implements IHideableElement {

    private static final Logger LOGGER = LogManager.getLogger();

    public WidgetMeta widgetMeta;
    public int originalX;
    public int originalY;
    public int originalWidth;
    public int originalHeight;
    public boolean vanillaButtonHidden = false;
    public int automatedButtonClicks = 0;
    protected boolean automatedButtonClicksDone = false;

    public VanillaWidgetElement(ElementBuilder<ButtonElement, ButtonEditorElement> builder) {
        super(builder);
    }

    @SuppressWarnings("all")
    @Override
    protected void renderElementWidget(@NotNull PoseStack pose, int mouseX, int mouseY, float partial) {
        ((CustomizableWidget)this.button).setHiddenFancyMenu((isEditor() || this.isCopyrightButton()) ? false : this.vanillaButtonHidden);
        super.renderElementWidget(pose, mouseX, mouseY, partial);
    }

    @Override
    public @Nullable List<GuiEventListener> getWidgetsToRegister() {
        return null;
    }

    @Override
    protected void renderTick() {

        super.renderTick();

        if (this.button == null) return;

        //Auto-click the vanilla button on menu load
        if (!isEditor() && !this.automatedButtonClicksDone && (this.automatedButtonClicks > 0)) {
            for (int i = 0; i < this.automatedButtonClicks; i++) {
                this.button.onClick(this.button.getX() + 1, this.button.getY() + 1);
            }
            this.automatedButtonClicksDone = true;
        }

    }

    @Override
    protected void updateLabels() {
        if (this.button == null) return;
        ((CustomizableWidget)this.button).setCustomLabelFancyMenu((this.label != null) ? buildComponent(this.label) : null);
        ((CustomizableWidget)this.button).setHoverLabelFancyMenu((this.hoverLabel != null) ? buildComponent(this.hoverLabel) : null);
    }

    @Override
    public int getAbsoluteX() {
        if ((this.button != null) && (this.anchorPoint == ElementAnchorPoints.VANILLA)) {
            int bX = this.posOffsetX;
            this.posOffsetX = this.originalX;
            int x = super.getAbsoluteX();
            this.posOffsetX = bX;
            return x;
        }
        return super.getAbsoluteX();
    }

    @Override
    public int getAbsoluteY() {
        if ((this.button != null) && (this.anchorPoint == ElementAnchorPoints.VANILLA)) {
            int bY = this.posOffsetY;
            this.posOffsetY = this.originalY;
            int y = super.getAbsoluteY();
            this.posOffsetY = bY;
            return y;
        }
        return super.getAbsoluteY();
    }

    @Override
    public int getAbsoluteWidth() {
        if ((this.button != null) && ((this.anchorPoint == ElementAnchorPoints.VANILLA) || (this.baseWidth == 0))) {
            this.baseWidth = this.originalWidth;
            int w = super.getAbsoluteWidth();
            this.baseWidth = 0;
            return w;
        }
        return super.getAbsoluteWidth();
    }

    @Override
    public int getAbsoluteHeight() {
        if ((this.button != null) && ((this.anchorPoint == ElementAnchorPoints.VANILLA) || (this.baseHeight == 0))) {
            this.baseHeight = this.originalHeight;
            int h = super.getAbsoluteHeight();
            this.baseHeight = 0;
            return h;
        }
        return super.getAbsoluteHeight();
    }

    @Override
    public @NotNull String getInstanceIdentifier() {
        if (this.widgetMeta != null) {
            return "vanillabtn:" + this.widgetMeta.getIdentifier();
        }
        return super.getInstanceIdentifier();
    }

    public void setVanillaButton(WidgetMeta data) {
        this.widgetMeta = data;
        this.button = data.getWidget();
        this.originalX = this.button.x;
        this.originalY = this.button.y;
        this.originalWidth = this.button.getWidth();
        this.originalHeight = this.button.getHeight();
    }

    @Override
    public boolean isHidden() {
        if (this.isCopyrightButton()) return false;
        return this.vanillaButtonHidden;
    }

    @Override
    public void setHidden(boolean hidden) {
        if (this.isCopyrightButton()) hidden = false;
        this.vanillaButtonHidden = hidden;
    }

    public boolean isCopyrightButton() {
        if (this.widgetMeta == null) return false;
        String compId = this.widgetMeta.getUniversalIdentifier();
        return ((compId != null) && compId.equals("mc_titlescreen_copyright_button"));
    }

}
package de.keksuccino.fancymenu.customization.loadingrequirement.requirements.world.gamemode;

import de.keksuccino.fancymenu.customization.loadingrequirement.LoadingRequirement;
import de.keksuccino.fancymenu.rendering.ui.texteditor.TextEditorFormattingRule;
import de.keksuccino.fancymenu.utils.LocalizationUtils;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.GameType;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class IsCreativeRequirement extends LoadingRequirement {

    public IsCreativeRequirement() {
        super("fancymenu_visibility_requirement_is_creative");
    }

    @Override
    public boolean hasValue() {
        return false;
    }

    @Override
    public boolean isRequirementMet(@Nullable String value) {

        if (Minecraft.getInstance().level != null) {
            LocalPlayer p = Minecraft.getInstance().player;
            ClientPacketListener l = Minecraft.getInstance().getConnection();
            if (l != null) {
                PlayerInfo playerinfo = l.getPlayerInfo(p.getGameProfile().getId());
                if (playerinfo != null) {
                    if (playerinfo.getGameMode() == GameType.CREATIVE) {
                        return true;
                    }
                }
            }
        }

        return false;

    }

    @Override
    public String getDisplayName() {
        return I18n.get("fancymenu.helper.visibilityrequirement.gamemode.is_creative");
    }

    @Override
    public List<String> getDescription() {
        return Arrays.asList(LocalizationUtils.splitLocalizedStringLines(I18n.get("fancymenu.helper.visibilityrequirement.gamemode.is_creative.desc")));
    }

    @Override
    public String getCategory() {
        return I18n.get("fancymenu.editor.loading_requirement.category.world");
    }

    @Override
    public String getValueDisplayName() {
        return null;
    }

    @Override
    public String getValuePreset() {
        return null;
    }

    @Override
    public List<TextEditorFormattingRule> getValueFormattingRules() {
        return null;
    }

}
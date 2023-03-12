package com.domain.redstonetools.gui.screens;

import com.domain.redstonetools.macros.MacroManager;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class MacrosScreen extends LightweightGuiDescription {

    final Screen last;
    final MacroManager macroManager = MacroManager.get();

    public MacrosScreen(Screen last) {
        this.last = last;

        setFullscreen(true);
        setTitleVisible(false); // draw big title because epic

        WGridPanel root = new WGridPanel();
        setRootPanel(root);

        WButton button = new WButton(Text.of("Back"));
        root.add(button, 1, 1, 4, 2);
        button.setOnClick(() -> {
            MinecraftClient.getInstance().setScreenAndRender(last);
        });
    }

    /*
        big title code:

        // draw title
        matrices.push();
        matrices.translate(20, 20, 0);
        matrices.scale(3f, 3f, 1f);
        textRenderer.drawWithShadow(matrices, Text.of("Macros"), 0, 0, 0xffffff);
        matrices.pop();
     */

}

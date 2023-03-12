package com.domain.redstonetools.gui;

import com.domain.redstonetools.macros.Macro;
import com.domain.redstonetools.macros.MacroManager;
import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WListPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.*;

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

        // create macro list
        WListPanel<Macro, MacroWidget> macroList = new WListPanel<>(macroManager.getMacros(), MacroWidget::new,
                (macro, macroWidget) -> macroWidget.updateForMacro(macro));
        macroList.setBackgroundPainter(BackgroundPainter.createColorful(new Color(0, 0, 0, 180).getAlpha()));
        macroList.setListItemHeight(30);
        root.add(macroList, 1, 2, 8, 4);
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

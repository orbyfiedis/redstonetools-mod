package com.domain.redstonetools.gui;

import com.domain.redstonetools.macros.Macro;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import net.minecraft.text.Text;

public class MacroWidget extends WPlainPanel {

    protected Macro macro;

    public MacroWidget() {
        setSize(400, 30);
    }

    public MacroWidget updateForMacro(Macro macro) {
        this.macro = macro;

        // update widgets
        add(new WLabel(Text.of(macro.getName())), 0, 0, 200, 30);

        return this;
    }

}

package com.domain.redstonetools.macros;

import com.domain.redstonetools.RedstoneToolsClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MacroManager {

    static final MacroManager INSTANCE = new MacroManager();

    public static MacroManager get() {
        return INSTANCE;
    }

    ////////////////////////////////////////////

    private final Map<String, Macro> macros = new LinkedHashMap<>();

    public Map<String, Macro> getMacros() {
        return macros;
    }

    public void addMacro(Macro macro) {
        macros.put(macro.name, macro);
    }

    /* Persistence */

    public void saveAll() {
        // TODO
    }

    public void loadAll() {
        RedstoneToolsClient.LOGGER.info("Loading persistent macros");
        // TODO
    }

}

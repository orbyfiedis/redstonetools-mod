package com.domain.redstonetools.macros;

import com.domain.redstonetools.RedstoneToolsClient;

import javax.crypto.Mac;
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

    private final List<Macro> macroList = new ArrayList<>();

    {
        // init test macros
        macroList.add(new Macro("Test1"));
        macroList.add(new Macro("Test2"));
    }

    public List<Macro> getMacros() {
        return macroList;
    }

    public void addMacro(Macro macro) {
        macroList.add(macro);
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

package com.domain.redstonetools.macros;

import com.domain.redstonetools.macros.actions.Action;
import com.domain.redstonetools.macros.triggers.Trigger;

import java.util.List;

public class Macro {

    public Macro(String name) {
        this.name = name;
    }

    final String name;
    boolean enabled;
    List<Trigger> triggers;
    List<Action> actions;

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Macro setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public List<Trigger> getTriggers() {
        return triggers;
    }

    public Macro setTriggers(List<Trigger> triggers) {
        this.triggers = triggers;
        return this;
    }

    public List<Action> getActions() {
        return actions;
    }

    public Macro setActions(List<Action> actions) {
        this.actions = actions;
        return this;
    }

    public void update() {
        if (!enabled) {
            return;
        }

        for (Trigger trigger : triggers) {
            handleTrigger(trigger);
        }
    }

    private void handleTrigger(Trigger trigger) {
        while (trigger.shouldBeHandled()) {
            runActions();

            trigger.handle();
        }
    }

    private void runActions() {
        for (Action action : actions) {
            action.run();
        }
    }

}

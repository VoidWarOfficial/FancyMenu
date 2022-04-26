//TODO übernehmen
package de.keksuccino.fancymenu.api.visibilityrequirements;

import de.keksuccino.fancymenu.FancyMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisibilityRequirementRegistry {

    protected static Map<String, VisibilityRequirement> requirements = new HashMap<>();

    /**
     * Register your custom visibility requirements here.
     */
    public static void registerRequirement(VisibilityRequirement requirement) {
        if (requirement != null) {
            if (requirement.getIdentifier() != null) {
                if (requirements.containsKey(requirement.getIdentifier())) {
                    FancyMenu.LOGGER.warn("[FANCYMENU] WARNING! A visibility requirement with the identifier '" + requirement.getIdentifier() + "' is already registered! Overriding requirement!");
                }
                requirements.put(requirement.getIdentifier(), requirement);
            } else {
                FancyMenu.LOGGER.error("[FANCYMENU] ERROR! Visibility requirement identifier cannot be null for VisibilityRequirements!");
            }
        }
    }

    /**
     * Unregister a previously added visibility requirement.
     */
    public static void unregisterRequirement(String requirementIdentifier) {
        requirements.remove(requirementIdentifier);
    }

    public static List<VisibilityRequirement> getRequirements() {
        List<VisibilityRequirement> l = new ArrayList<>();
        l.addAll(requirements.values());
        return l;
    }

    public static VisibilityRequirement getRequirement(String requirementIdentifier) {
        return requirements.get(requirementIdentifier);
    }

}
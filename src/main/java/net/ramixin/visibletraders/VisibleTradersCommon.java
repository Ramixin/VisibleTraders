package net.ramixin.visibletraders;

import net.minecraft.resources.Identifier;
import net.ramixin.visibletraders.commonapi.LoaderMedium;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisibleTradersCommon {

    public static final String MOD_ID = "visibletraders";
    public static final String MOD_NAME = "Visible Traders";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private static LoaderMedium medium;

    public static void onInitialize(LoaderMedium medium) {
        VisibleTradersCommon.medium = medium;
    }

    public static LoaderMedium getMedium() {
        if(medium == null) throw new IllegalStateException("VisibleTraders loader medium not initialized");
        return medium;
    }

    public static Identifier id(String id) {
        return Identifier.fromNamespaceAndPath(MOD_ID, id);
    }

}

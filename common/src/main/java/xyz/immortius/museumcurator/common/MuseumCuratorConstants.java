package xyz.immortius.museumcurator.common;

import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Constants for Museum Curator - may vary by mod system
 */
public final class MuseumCuratorConstants {

    public static final String EXHIBIT_DATA_PATH = "museumexhibits";

    public static final Logger LOGGER = LogManager.getLogger(MuseumCuratorConstants.MOD_ID);

    public static final String MOD_ID = "museumcurator";

    public static final String DEFAULT_CONFIG_PATH = "defaultconfigs";
    public static final String CONFIG_FILE = MOD_ID + ".toml";
    public static final Identifier WRITING_SOUND_ID = Identifier.fromNamespaceAndPath(MOD_ID, "writing");

    public static final Identifier LOG_ON_MESSAGE_ID = Identifier.fromNamespaceAndPath(MOD_ID, "logon_message");
    public static final Identifier CHECKLIST_UPDATE_MESSAGE_ID = Identifier.fromNamespaceAndPath(MOD_ID, "checklist_update_message");
    public static final Identifier CHECKLIST_CHANGE_REQUEST_MESSAGE_ID = Identifier.fromNamespaceAndPath(MOD_ID, "checklist_change_request");

    private MuseumCuratorConstants() {
    }
}
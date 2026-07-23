package dev.oolist.ehud.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.oolist.ehud.network.AdminUpdatePayload;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

final class VersionKeyBindings {
    private VersionKeyBindings() { }

    static KeyMapping[] create() {
        KeyMapping.Category category = KeyMapping.Category.register(AdminUpdatePayload.TYPE.id());
        return new KeyMapping[]{
                new KeyMapping("key.ehud.open_config", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, category),
                new KeyMapping("key.ehud.inspect", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, category),
                new KeyMapping("key.ehud.pin", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, category)
        };
    }
}

package dev.oolist.ehud.client.screen;

import dev.oolist.ehud.client.config.ConfigSession;
import dev.oolist.ehud.client.config.FeatureCatalog;
import dev.oolist.ehud.client.config.HudFeature;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

final class FeatureListScreen extends Screen {
    private static final int PAGE_SIZE = 7;
    private final Screen parent;
    private final ConfigSession session;
    private final boolean favorites;
    private int page;

    FeatureListScreen(Screen parent, ConfigSession session, boolean favorites) {
        super(Component.literal(favorites ? "Favorite settings" : "Recently changed"));
        this.parent = parent;
        this.session = session;
        this.favorites = favorites;
    }

    @Override
    protected void init() {
        clearWidgets();
        List<HudFeature> features = features();
        int start = page * PAGE_SIZE;
        for (int row = 0; row < PAGE_SIZE && start + row < features.size(); row++) {
            HudFeature feature = features.get(start + row);
            addRenderableWidget(Button.builder(Component.literal(feature.module().title() + " | " + feature.title()),
                            button -> minecraft.setScreen(new HudElementScreen(this, session, feature)))
                    .bounds(width / 2 - 210, 56 + row * 30, 420, 20).build());
        }
        int bottom = height - 27;
        addRenderableWidget(Button.builder(Component.literal("Previous"), button -> {
            page = Math.max(0, page - 1);
            rebuildWidgets();
        }).bounds(width / 2 - 160, bottom, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(width / 2 - 45, bottom, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Next"), button -> {
            page = Math.min(Math.max(0, (features.size() - 1) / PAGE_SIZE), page + 1);
            rebuildWidgets();
        }).bounds(width / 2 + 70, bottom, 90, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        EHudScreenStyle.header(graphics, font, width, session.working(),
                favorites ? "FAVORITES" : "RECENTLY CHANGED");
        super.render(graphics, mouseX, mouseY, delta);
        if (features().isEmpty()) {
            graphics.drawCenteredString(font, favorites
                            ? "Use the + button beside a setting to favorite it."
                            : "Changed settings will appear here.",
                    width / 2, 86, 0xFF9FD8AE);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private List<HudFeature> features() {
        List<String> keys = favorites ? session.working().favoriteSettings : session.working().recentSettings;
        return keys.stream().map(key -> FeatureCatalog.all(true).stream()
                        .filter(feature -> feature.key().equals(key)).findFirst().orElse(null))
                .filter(java.util.Objects::nonNull).toList();
    }
}

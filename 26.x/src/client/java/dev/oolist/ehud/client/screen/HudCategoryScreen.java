package dev.oolist.ehud.client.screen;

import dev.oolist.ehud.client.config.ConfigSession;
import dev.oolist.ehud.client.config.FeatureCatalog;
import dev.oolist.ehud.client.config.HudFeature;
import dev.oolist.ehud.client.config.HudModule;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import dev.oolist.ehud.client.network.ClientServerState;

final class HudCategoryScreen extends Screen {
    private static final int PAGE_SIZE = 6;
    private final Screen parent;
    private final ConfigSession session;
    private final HudModule module;
    private int page;
    private String query = "";
    private EditBox search;

    HudCategoryScreen(Screen parent, ConfigSession session, HudModule module) {
        super(Component.literal(module.title()));
        this.parent = parent;
        this.session = session;
        this.module = module;
    }

    @Override
    protected void init() {
        clearWidgets();
        List<HudFeature> features = features();
        search = new EditBox(font, width / 2 - 156, 42, 220, 20, Component.literal("Search settings"));
        search.setHint(Component.literal("Search this category"));
        search.setValue(query);
        addRenderableWidget(search);
        addRenderableWidget(Button.builder(Component.literal("Search"), button -> {
            query = search.getValue().trim();
            page = 0;
            rebuildWidgets();
        }).bounds(width / 2 + 70, 42, 78, 20).build());
        int left = width / 2 - 250;
        int top = 70;
        for (int row = 0; row < PAGE_SIZE; row++) {
            int index = page * PAGE_SIZE + row;
            if (index >= features.size()) break;
            HudFeature feature = features.get(index);
            int y = top + row * 39;
            boolean enabled = session.working().isLineEnabled(feature.key());
            Button toggle = Button.builder(Component.literal(enabled ? "ON" : "OFF"), button -> {
                session.working().settingsFor(feature.key()).enabled = !session.working().isLineEnabled(feature.key());
                session.changed();
                rebuildWidgets();
            }).bounds(left, y, 44, 20).build();
            Button favorite = Button.builder(Component.literal(
                    session.working().favoriteSettings.contains(feature.key()) ? "*" : "+"), button -> {
                if (!session.working().favoriteSettings.remove(feature.key())) {
                    session.working().favoriteSettings.add(feature.key());
                }
                session.changed();
                rebuildWidgets();
            }).bounds(left + 49, y, 30, 20).build();
            Button customize = Button.builder(Component.literal(feature.title()), button ->
                    VersionClientUi.setScreen(minecraft, new HudElementScreen(this, session, feature)))
                    .bounds(left + 84, y, 416, 20).build();
            if (ClientServerState.disabledByOperator(module) || serverUnavailable()) {
                toggle.active = false;
                favorite.active = false;
                customize.active = false;
            }
            addRenderableWidget(toggle);
            addRenderableWidget(favorite);
            addRenderableWidget(customize);
        }

        int bottom = height - 27;
        addRenderableWidget(Button.builder(Component.literal("< Previous"), button -> {
            page = Math.max(0, page - 1); rebuildWidgets();
        }).bounds(width / 2 - 180, bottom, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(width / 2 - 45, bottom, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Next >"), button -> {
            page = Math.min(Math.max(0, (features.size() - 1) / PAGE_SIZE), page + 1); rebuildWidgets();
        }).bounds(width / 2 + 90, bottom, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal(session.working().modules.getOrDefault(module, true)
                ? "Category enabled" : "Category disabled"), button -> {
            session.working().modules.put(module, !session.working().modules.getOrDefault(module, true));
            session.changed(); rebuildWidgets();
        }).bounds(width - 142, 8, 134, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        EHudScreenStyle.background(graphics, width, height);
        EHudScreenStyle.header(graphics, font, width, session.working(), module.title().toUpperCase());
        if (ClientServerState.disabledByOperator(module)) {
            graphics.centeredText(font, "Disabled by operator", width / 2, 42, 0xFFFF4D4D);
        } else if (serverUnavailable()) {
            graphics.centeredText(font, "Requires E HUD on this server", width / 2, 64, 0xFF777777);
        }
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        List<HudFeature> features = features();
        int start = page * PAGE_SIZE;
        for (int row = 0; row < PAGE_SIZE && start + row < features.size(); row++) {
            HudFeature feature = features.get(start + row);
            graphics.text(font, feature.description(), width / 2 - 160, 92 + row * 39,
                    0xFF94A89A, false);
        }
        graphics.centeredText(font, "Page " + (page + 1) + " / "
                + Math.max(1, (features.size() + PAGE_SIZE - 1) / PAGE_SIZE), width / 2, height - 42,
                0xFF9FD8AE);
    }

    @Override
    public void onClose() {
        VersionClientUi.setScreen(minecraft, parent);
    }

    private List<HudFeature> features() {
        return FeatureCatalog.forModule(module, session.working().advancedMode).stream()
                .filter(feature -> query.isBlank()
                        || feature.title().toLowerCase().contains(query.toLowerCase())
                        || feature.description().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    private boolean serverUnavailable() {
        return module == HudModule.MULTIPLAYER && minecraft != null && minecraft.level != null
                && !ClientServerState.available();
    }
}

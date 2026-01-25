package com.SmartEntityRender.video_settings.sidebar;

import com.SmartEntityRender.video_settings.VideoSettings;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class VideoSettingsSidebar {
    private VideoSettingsSidebar() {
    }

    public static final class Panel implements Drawable {
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final TextRenderer textRenderer;

        public Panel(
                int x,
                int y,
                int width,
                int height,
                TextRenderer textRenderer) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.textRenderer = textRenderer;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            int windowH = MinecraftClient.getInstance().getWindow().getScaledHeight();
            int panelH = Math.max(this.height, Math.max(0, windowH - this.y));

            int top = 0x880D1117;
            int bottom = 0x88090B10;
            context.fillGradient(x, y, x + width, y + panelH, top, bottom);

            int border = 0xAA24262C;
            context.fill(x, y, x + width, y + 1, border);
            context.fill(x, y + panelH - 1, x + width, y + panelH, border);
            context.fill(x, y, x + 1, y + panelH, border);
            context.fill(x + width - 1, y, x + width, y + panelH, border);

            String titleText = "Video Settings";
            int titleY = y + 8;
            int titleW = textRenderer.getWidth(titleText);
            int titleX = x + (width - titleW) / 2;
            context.drawText(textRenderer, Text.literal(titleText), titleX, titleY, 0xFFEDEDED, false);
            int lineY = titleY + 12;
            context.fill(x + 8, lineY, x + width - 8, lineY + 1, 0xFF2A2D36);

            String modLabel = getModLabel();
            int footerY = y + panelH - 12;
            int maxFooterW = Math.max(0, width - 16);
            String footerText = textRenderer.trimToWidth(modLabel, maxFooterW);
            int footerTextW = textRenderer.getWidth(footerText);
            int footerX = x + (width - footerTextW) / 2;
            context.drawText(textRenderer, Text.literal(footerText), footerX, footerY, 0xFF6B7280, false);
        }

        private String getModLabel() {
            return FabricLoader.getInstance()
                    .getModContainer("oxygen")
                    .map(container -> "Oxygen v" + container.getMetadata().getVersion().getFriendlyString())
                    .orElse("Oxygen");
        }
    }

    public static final class TabButton extends ClickableWidget {
        private final VideoSettings.Section section;
        private final Supplier<VideoSettings.Section> selectedSection;
        private final Consumer<VideoSettings.Section> onSelect;

        public TabButton(
                int x,
                int y,
                int width,
                int height,
                Text message,
                VideoSettings.Section section,
                Supplier<VideoSettings.Section> selectedSection,
                Consumer<VideoSettings.Section> onSelect) {
            super(x, y, width, height, message);
            this.section = section;
            this.selectedSection = selectedSection;
            this.onSelect = onSelect;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean selected = selectedSection.get() == section;

            int x0 = getX();
            int y0 = getY();
            int x1 = x0 + width;
            int y1 = y0 + height;

            int bg = selected ? 0x99111827 : 0x8011131A;
            int bgHover = selected ? 0xB91A2A44 : 0x99181A22;
            context.fill(x0, y0, x1, y1, this.hovered ? bgHover : bg);

            int border = selected ? 0x9960A5FA : 0xAA2A2D36;
            context.fill(x0, y0, x1, y0 + 1, border);
            context.fill(x0, y1 - 1, x1, y1, border);
            context.fill(x0, y0, x0 + 1, y1, border);
            context.fill(x1 - 1, y0, x1, y1, border);

            if (selected) {
                context.fill(x0, y0, x0 + 3, y1, 0x9960A5FA);
            }

            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            int textColor = selected ? 0xFFF3F4F6 : 0xFFD1D5DB;
            if (!this.active) {
                textColor = 0xFF6B7280;
            }

            int textX = x0 + (selected ? 10 : 8);
            int textY = y0 + (height - 8) / 2;
            int maxTextW = width - (selected ? 14 : 12);
            String trimmed = tr.trimToWidth(getMessage().getString(), Math.max(0, maxTextW));
            context.drawText(tr, Text.literal(trimmed), textX, textY, textColor, false);
        }

        @Override
        public void onClick(Click click, boolean ctrlDown) {
            if (!this.active || !this.visible) {
                return;
            }
            onSelect.accept(section);
            super.onClick(click, ctrlDown);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            builder.put(net.minecraft.client.gui.screen.narration.NarrationPart.TITLE, getMessage());
        }
    }

    /**
     * Special Addons button with unique design
     */
    public static final class AddonsButton extends ClickableWidget {
        private final Runnable onPress;

        public AddonsButton(int x, int y, int width, int height, Runnable onPress) {
            super(x, y, width, height, Text.literal("Addons"));
            this.onPress = onPress;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int x0 = getX();
            int y0 = getY();
            int x1 = x0 + width;
            int y1 = y0 + height;

            // Simple dark background
            int bg = this.hovered ? 0xCC1F2937 : 0xAA111827;
            context.fill(x0, y0, x1, y1, bg);

            // Simple border
            int border = this.hovered ? 0xFF60A5FA : 0xFF374151;
            context.fill(x0, y0, x1, y0 + 1, border);
            context.fill(x0, y1 - 1, x1, y1, border);
            context.fill(x0, y0, x0 + 1, y1, border);
            context.fill(x1 - 1, y0, x1, y1, border);

            // Simple accent line on left
            if (this.hovered) {
                context.fill(x0, y0, x0 + 2, y1, 0xFF60A5FA);
            }

            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            String text = "Addons";
            int textW = tr.getWidth(text);
            int textX = x0 + (width - textW) / 2;
            int textY = y0 + (height - 8) / 2;

            int textColor = this.hovered ? 0xFF60A5FA : 0xFFD1D5DB;
            context.drawText(tr, Text.literal(text), textX, textY, textColor, false);
        }

        @Override
        public void onClick(Click click, boolean ctrlDown) {
            if (!this.active || !this.visible) {
                return;
            }
            if (onPress != null) {
                onPress.run();
            }
            super.onClick(click, ctrlDown);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            builder.put(net.minecraft.client.gui.screen.narration.NarrationPart.TITLE,
                    Text.literal("Addons - Special Features"));
        }
    }
}

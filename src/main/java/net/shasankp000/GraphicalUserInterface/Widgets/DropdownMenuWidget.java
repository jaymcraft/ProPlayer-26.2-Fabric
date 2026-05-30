package net.shasankp000.GraphicalUserInterface.Widgets;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class DropdownMenuWidget extends AbstractWidget {
    private static final Logger LOGGER = LoggerFactory.getLogger("DropdownWidget");
    private List<String> options;
    private boolean isOpen;
    private int selectedIndex = -1;
    private int hoveredIndex = -1;
    private String selectedOption = "";
    private final int rowHeight = 14;
    private final int maxVisibleOptions = 10;

    public DropdownMenuWidget(int x, int y, int width, int height, Component message, List<String> options) {
        super(x, y, width, height, message);
        this.options = (options != null) ? options : new ArrayList<>();
        this.isOpen = false;
    }

    public boolean isExpanded() {
        return this.isOpen;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        Font tr = Minecraft.getInstance().font;

        // Draw main box
        int buttonColor = this.isHovered() ? 0xFF404040 : 0xFF202020;
        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFFFFFFFF);
        context.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1,
                buttonColor);

        String label = !selectedOption.isEmpty() ? selectedOption : getMessage().getString();
        context.centeredText(tr, label, this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 0xFFFFFF);

        if (isOpen) {
            int listSize = Math.min(options.size(), maxVisibleOptions);
            int totalListHeight = Math.max(1, listSize) * rowHeight;

            context.fill(this.getX(), this.getY() + this.height, this.getX() + this.width,
                    this.getY() + this.height + totalListHeight, 0xFF101010);

            if (options.isEmpty()) {
                context.text(Minecraft.getInstance().font, "No models found", this.getX() + 5,
                        this.getY() + this.height + 5, 0xFFFF8080);
                return;
            }

            for (int i = 0; i < listSize; i++) {
                boolean isHovered = mouseX >= this.getX() && mouseX < this.getX() + this.width &&
                        mouseY >= this.getY() + this.height + (i * rowHeight) &&
                        mouseY < this.getY() + this.height + ((i + 1) * rowHeight);

                int itemColor = isHovered ? 0xFF00FF00 : 0xFFFFFFFF; // Green if hovered, otherwise white
                context.fill(this.getX(), this.getY() + this.height + (i * rowHeight), this.getX() + this.width,
                        this.getY() + this.height + ((i + 1) * rowHeight), 0xFF888888); // Item background
                context.text(Minecraft.getInstance().font, options.get(i), this.getX() + 5,
                        this.getY() + this.height + (i * rowHeight) + 5, itemColor);
            }
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean isDouble) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (!this.active || !this.visible)
            return false;

        boolean clickedMain = mouseX >= this.getX() && mouseX < this.getX() + this.width &&
                mouseY >= this.getY() && mouseY < this.getY() + this.height;

        if (isOpen) {
            int listSize = Math.min(options.size(), maxVisibleOptions);
            for (int i = 0; i < listSize; i++) {
                int optionY = this.getY() + this.height + (i * rowHeight);

                if (mouseX >= this.getX() && mouseX <= this.getX() + this.width &&
                        mouseY >= optionY && mouseY < optionY + rowHeight) {

                    this.selectedIndex = i;
                    this.selectedOption = options.get(i);
                    this.isOpen = false;
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    return true;
                }
            }
            this.isOpen = false;
            return true;
        }

        if (clickedMain) {
            this.isOpen = !this.isOpen;
            if (this.isOpen)
                this.hoveredIndex = 0;
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (!this.active || !this.visible || !this.isOpen)
            return super.keyPressed(event);

        int listSize = Math.min(options.size(), maxVisibleOptions);
        if (listSize == 0) {
            return super.keyPressed(event);
        }

        if (event.key() == GLFW.GLFW_KEY_DOWN) {
            this.hoveredIndex = (this.hoveredIndex + 1) % listSize;
            return true;
        } else if (event.key() == GLFW.GLFW_KEY_UP) {
            this.hoveredIndex = (this.hoveredIndex - 1 + listSize) % listSize;
            return true;
        } else if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) {
            if (hoveredIndex >= 0 && hoveredIndex < options.size()) {
                this.selectedIndex = hoveredIndex;
                this.selectedOption = options.get(hoveredIndex);
                this.isOpen = false;
                this.playDownSound(Minecraft.getInstance().getSoundManager());
            }
            return true;
        } else if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.isOpen = false;
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        builder.add(NarratedElementType.TITLE, getMessage());
    }

    public void updateOptions(List<String> newOptions) {
        this.options = new ArrayList<>(newOptions);
        this.selectedIndex = this.options.indexOf(this.selectedOption);
        this.hoveredIndex = this.options.isEmpty() ? -1 : 0;
    }

    public void setOpen(boolean open) {
        this.isOpen = open;
        if (open && !this.options.isEmpty()) {
            this.hoveredIndex = 0;
        }
    }

    public String getSelectedOption() {
        return selectedOption;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        int expandedHeight = this.height;
        if (this.isOpen) {
            expandedHeight += Math.max(1, Math.min(options.size(), maxVisibleOptions)) * rowHeight;
        }
        return mouseX >= this.getX() && mouseX < this.getX() + this.width
                && mouseY >= this.getY() && mouseY < this.getY() + expandedHeight;
    }
}

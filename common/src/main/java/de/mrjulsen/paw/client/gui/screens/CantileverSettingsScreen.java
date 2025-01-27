package de.mrjulsen.paw.client.gui.screens;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;

import de.mrjulsen.paw.PantographsAndWires;
import de.mrjulsen.paw.block.abstractions.AbstractCantileverBlock;
import de.mrjulsen.paw.block.abstractions.AbstractCantileverBlock.ECantileverInsulatorsPlacement;
import de.mrjulsen.paw.block.abstractions.AbstractCantileverBlock.ECantileverRegistrationArmType;
import de.mrjulsen.paw.client.gui.ModGuiUtils;
import de.mrjulsen.paw.client.gui.widgets.DLCreateEnumSlider;
import de.mrjulsen.paw.client.gui.widgets.DLCreateSlider;
import de.mrjulsen.paw.item.CantileverBlockItem;
import de.mrjulsen.paw.registry.ModBlocks;
import de.mrjulsen.paw.registry.ModNetworkAccessor;
import de.mrjulsen.paw.registry.ModNetworkAccessor.CantileverSettingsData;
import de.mrjulsen.paw.registry.ModBlocks.CantileverKey;
import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.data.Cache;
import de.mrjulsen.mcdragonlib.util.MathUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.accessor.DataAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class CantileverSettingsScreen extends DLScreen {

    private static final ResourceLocation TEXTURE = new ResourceLocation(PantographsAndWires.MOD_ID, "textures/gui/cantilever_settings.png");
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int GUI_WIDTH = 224;
    private static final int GUI_HEIGHT = 160;

    private final MutableComponent textSize = TextUtils.translate("gui." + PantographsAndWires.MOD_ID + ".cantilever_settings.size");

    private int guiLeft;
    private int guiTop;

    private int size = 3;
    private ECantileverRegistrationArmType type = ECantileverRegistrationArmType.INNER;
    private ECantileverInsulatorsPlacement insulatorPlacement = ECantileverInsulatorsPlacement.BACK;

    private final ItemStack stack;
    private final Cache<BlockState> stateCache;

    public CantileverSettingsScreen(ItemStack stack) {
        super(TextUtils.translate("gui." + PantographsAndWires.MOD_ID + ".cantilever_settings.title"));
        if (!(stack.getItem() instanceof CantileverBlockItem)) {
            throw new IllegalArgumentException(stack.getItem() + " is not a CantileverBlockItem.");
        }

        this.size = CantileverBlockItem.getSize(stack);
        this.type = CantileverBlockItem.getCantileverType(stack);
        this.insulatorPlacement = CantileverBlockItem.getInsulatorPlacement(stack);

        this.stack = stack;
        this.stateCache = new Cache<>(() -> {
            return ModBlocks.getCantilever(new CantileverKey(size, ((CantileverBlockItem<?>)stack.getItem()).getInsulatorType())).get().defaultBlockState()
                .setValue(AbstractCantileverBlock.REGISTRATION_ARM, type)
                .setValue(AbstractCantileverBlock.INSULATORS_PLACEMENT, insulatorPlacement)
            ;
        });
    }

    @Override
    public void onClose() {
        CantileverSettingsData data = new CantileverSettingsData((byte)size, type, insulatorPlacement);
        CantileverBlockItem.setNbt(stack, data);
        DataAccessor.getFromServer(data, ModNetworkAccessor.UPDATE_CANTILEVER_SETTINGS, $ -> {});
        super.onClose();
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = width() / 2 - GUI_WIDTH / 2;
        this.guiTop = height() / 2 - GUI_HEIGHT / 2;

        int w = (AbstractCantileverBlock.MAX_SIZE - AbstractCantileverBlock.MIN_SIZE + 1) * 7;
        addRenderableWidget(new DLCreateSlider(this, guiLeft + GUI_WIDTH - 10 - w, guiTop + GUI_HEIGHT - 18, w, textSize, TextUtils.empty(), AbstractCantileverBlock.MIN_SIZE, AbstractCantileverBlock.MAX_SIZE, this.size, 1, 1, (s) -> {
            this.size = s.getValueInt();
            stateCache.clear();
        }));

        DLCreateEnumSlider<ECantileverRegistrationArmType> typeSlider = addRenderableWidget(new DLCreateEnumSlider<>(this, guiLeft + 10, guiTop + GUI_HEIGHT - 18, ECantileverRegistrationArmType.values().length * 10, ECantileverRegistrationArmType.class, this.type, (s, e) -> {
            this.type = e;
            stateCache.clear();
        }));

        addRenderableWidget(new DLCreateEnumSlider<>(this, guiLeft + 10 + typeSlider.width() + 16, guiTop + GUI_HEIGHT - 18, ECantileverInsulatorsPlacement.values().length * 10, ECantileverInsulatorsPlacement.class, this.insulatorPlacement, (s, e) -> {
            this.insulatorPlacement = e;
            stateCache.clear();
        }));
        
    }

    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderScreenBackground(graphics);
        GuiUtils.drawTexture(TEXTURE, graphics, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        GuiUtils.enableScissor(graphics, guiLeft + 3, guiTop + 3, GUI_WIDTH - 6, GUI_HEIGHT - 6);
        Lighting.setupForFlatItems();
        graphics.poseStack().pushPose();
        graphics.poseStack().setIdentity();
        graphics.poseStack().translate((double)width() / 2, guiTop + 3 + 16 + 50 - (MathUtils.clamp((size - 4), 0, 3) * 8), 200);
        graphics.poseStack().scale(24, 24, -24);
        graphics.poseStack().mulPose(Axis.ZP.rotationDegrees(180));
        graphics.poseStack().mulPose(Axis.YP.rotationDegrees((float)System.nanoTime() / 50000000f));
        graphics.poseStack().pushPose();
        graphics.poseStack().translate(-0.5f, 0, ((float)size - 1.5f) / 2f - 0.5f);
        MultiBufferSource.BufferSource multibuffersource$buffersource = this.minecraft.renderBuffers().bufferSource();        
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(stateCache.get(), graphics.poseStack(), multibuffersource$buffersource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        multibuffersource$buffersource.endBatch();
        graphics.poseStack().popPose();
        graphics.poseStack().popPose();
        Lighting.setupFor3DItems();
        GuiUtils.disableScissor(graphics);

        graphics.poseStack().pushPose();
        graphics.poseStack().translate(0, 0, 500);
        super.renderMainLayer(graphics, mouseX, mouseY, partialTicks);

        int halfWidth = font.width(title) / 2;
        ModGuiUtils.renderRoundedBox(graphics, width() / 2 - halfWidth - 3, guiTop + 6, halfWidth * 2 + 6, font.lineHeight + 3, 0x55000000);
        GuiUtils.drawString(graphics, font, width() / 2, guiTop + 8, title, 0xFFFFFFFF, EAlignment.CENTER, false);
        graphics.poseStack().popPose();
    }
}

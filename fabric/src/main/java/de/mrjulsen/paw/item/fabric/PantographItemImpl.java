package de.mrjulsen.paw.item.fabric;

import java.util.function.Consumer;
import java.util.function.Supplier;

import de.mrjulsen.paw.PantographsAndWires;
import de.mrjulsen.paw.item.PantographItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PantographItemImpl extends PantographItem {
	private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

	protected PantographItemImpl(Block block, Properties properties, boolean expanded) {
		super(block, properties, expanded);
	}

	public static PantographItem create(Block block, Properties properties, boolean expanded) {
        return new PantographItemImpl(block, properties, expanded);
	}

	@Override
	public void createRenderer(Consumer<Object> consumer) {
		consumer.accept(new RenderProvider() {
			private GeoItemRenderer<PantographItem> renderer = null;

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				if (this.renderer == null)
					this.renderer = new GeoItemRenderer<>(new DefaultedBlockGeoModel<>(new ResourceLocation(PantographsAndWires.MOD_ID, "pantograph")));

				return this.renderer;
			}
		});
	}

	@Override
	public Supplier<Object> getRenderProvider() {
		return this.renderProvider;
	}    
}

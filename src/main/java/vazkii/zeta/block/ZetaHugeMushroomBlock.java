package vazkii.zeta.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HugeMushroomBlock;
import vazkii.zeta.event.ZRegister;

public class ZetaHugeMushroomBlock extends HugeMushroomBlock implements IZetaBlock {
	protected final ZetaBlockProps props;

	public ZetaHugeMushroomBlock(@Nullable ZRegister event, ZetaBlockProps props) {
		super(props.getVanilla());
		this.props = props;
		props.register(this, event);
	}

	public ZetaHugeMushroomBlock(ZetaBlockProps props) {
		this(null, props);
	}

	@Override
	public ZetaBlockProps getProps() {
		return props;
	}

	@Override
	public void fillItemCategory(@Nonnull CreativeModeTab group, @Nonnull NonNullList<ItemStack> items) {
		if (isEnabled() || group == CreativeModeTab.TAB_SEARCH)
			super.fillItemCategory(group, items);
	}
}

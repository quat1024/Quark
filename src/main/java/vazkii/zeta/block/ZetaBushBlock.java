package vazkii.zeta.block;

import javax.annotation.Nonnull;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BushBlock;
import org.jetbrains.annotations.Nullable;
import vazkii.zeta.event.ZRegister;

public class ZetaBushBlock extends BushBlock implements IZetaBlock {
	protected final ZetaBlockProps props;

	public ZetaBushBlock(@Nullable ZRegister event, ZetaBlockProps props) {
		super(props.getVanilla());
		this.props = props;
		props.register(this, event);
	}

	public ZetaBushBlock(ZetaBlockProps props) {
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

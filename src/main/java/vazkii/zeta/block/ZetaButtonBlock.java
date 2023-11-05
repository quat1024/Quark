package vazkii.zeta.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ButtonBlock;
import vazkii.zeta.event.ZRegister;

public abstract class ZetaButtonBlock extends ButtonBlock implements IZetaBlock {
	protected final ZetaBlockProps props;

	public ZetaButtonBlock(@Nullable ZRegister event, ZetaBlockProps props) {
		super(false, props.getVanilla());
		this.props = props;
		props.register(this, event);
	}

	public ZetaButtonBlock(ZetaBlockProps props) {
		this(null, props);
	}

	@Override
	protected abstract SoundEvent getSound(boolean pressed);

	@Override
	public abstract int getPressDuration();

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

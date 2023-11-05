package vazkii.zeta.block;

import java.util.function.BooleanSupplier;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;
import vazkii.zeta.event.ZRegister;
import vazkii.zeta.module.IDisableable;
import vazkii.zeta.module.ZetaModule;

public interface IZetaBlock extends IDisableable<IZetaBlock> {

	ZetaBlockProps getProps();

	default void register(ZRegister event) {
		getProps().register(this, event);
	}

	default Block getBlock() {
		return (Block) this;
	}

	@Override
	default @Nullable ZetaModule getModule() {
		return getProps().module;
	}

	@Override
	default boolean doesConditionApply() {
		return getProps().condition.getAsBoolean();
	}

	@Override
	@Deprecated
	default IZetaBlock setCondition(BooleanSupplier condition) {
		//this.condition = condition;
		return this;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	default <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> thisType, BlockEntityType<E> targetType, BlockEntityTicker<? super E> ticker) {
		return targetType == thisType ? (BlockEntityTicker<A>) ticker : null;
	}

}

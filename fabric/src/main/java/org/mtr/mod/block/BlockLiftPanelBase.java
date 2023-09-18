package org.mtr.mod.block;

import org.mtr.mapping.holder.*;
import org.mtr.mapping.mapper.*;
import org.mtr.mod.Items;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class BlockLiftPanelBase extends BlockExtension implements ITripleBlock, DirectionHelper, BlockWithEntity {

	private final boolean isOdd;
	private final boolean isFlat;

	public BlockLiftPanelBase(boolean isOdd, boolean isFlat) {
		super(BlockHelper.createBlockSettings(true, blockState -> 5));
		this.isOdd = isOdd;
		this.isFlat = isFlat;
	}

	@Nonnull
	@Override
	public BlockState getStateForNeighborUpdate2(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		if (isOdd) {
			return ITripleBlock.updateShape(state, direction, neighborState.isOf(new Block(this)), () -> super.getStateForNeighborUpdate2(state, direction, neighborState, world, pos, neighborPos));
		} else {
			if (IBlock.getSideDirection(state) == direction && !neighborState.isOf(new Block(this))) {
				return Blocks.getAirMapped().getDefaultState();
			} else {
				return state;
			}
		}
	}

	@Override
	public BlockState getPlacementState2(ItemPlacementContext ctx) {
		final Direction direction = ctx.getPlayerFacing();
		if (isOdd) {
			return IBlock.isReplaceable(ctx, direction.rotateYClockwise(), 3) ? getDefaultState2().with(new Property<>(FACING.data), direction.data).with(new Property<>(SIDE.data), EnumSide.LEFT).with(new Property<>(ODD.data), false) : null;
		} else {
			return IBlock.isReplaceable(ctx, direction.rotateYClockwise(), 2) ? getDefaultState2().with(new Property<>(FACING.data), direction.data).with(new Property<>(SIDE.data), EnumSide.LEFT) : null;
		}
	}

	@Nonnull
	@Override
	public VoxelShape getOutlineShape2(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return IBlock.getVoxelShapeByDirection(0, 0, 0, 16, 16, isFlat ? 1 : 4, Direction.convert(state.get(new Property<>(FACING.data))));
	}

	@Override
	public void onPlaced2(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		if (!world.isClient()) {
			final Direction direction = IBlock.getStatePropertySafe(state, FACING);

			if (isOdd) {
				world.setBlockState(pos.offset(direction.rotateYClockwise()), getDefaultState2().with(new Property<>(FACING.data), direction.data).with(new Property<>(SIDE.data), EnumSide.RIGHT).with(new Property<>(ODD.data), true), 3);
				world.setBlockState(pos.offset(direction.rotateYClockwise(), 2), getDefaultState2().with(new Property<>(FACING.data), direction.data).with(new Property<>(SIDE.data), EnumSide.RIGHT).with(new Property<>(ODD.data), false), 3);
				world.updateNeighbors(pos.offset(direction.rotateYClockwise()), Blocks.getAirMapped());
				state.updateNeighbors(new WorldAccess(world.data), pos.offset(direction.rotateYClockwise()), 3);
			} else {
				world.setBlockState(pos.offset(direction.rotateYClockwise()), getDefaultState2().with(new Property<>(FACING.data), direction.data).with(new Property<>(SIDE.data), EnumSide.RIGHT), 3);
			}

			world.updateNeighbors(pos, Blocks.getAirMapped());
			state.updateNeighbors(new WorldAccess(world.data), pos, 3);
		}
	}

	@Override
	public void onBreak2(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		if (isOdd) {
			ITripleBlock.playerWillDestroy(world, pos, state, player, false);
		} else {
			if (IBlock.getStatePropertySafe(state, SIDE) == EnumSide.RIGHT) {
				IBlock.onBreakCreative(world, player, pos.offset(IBlock.getSideDirection(state)));
			}
		}
		super.onBreak2(world, pos, state, player);
	}

	@Nonnull
	@Override
	public ActionResult onUse2(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (world.isClient()) {
			return ActionResult.SUCCESS;
		} else {
			return player.isHolding(Items.LIFT_BUTTONS_LINK_CONNECTOR.get()) || player.isHolding(Items.LIFT_BUTTONS_LINK_REMOVER.get()) ? ActionResult.PASS : ActionResult.FAIL;
		}
	}

	@Override
	public void addTooltips(ItemStack stack, @Nullable BlockView world, List<MutableText> tooltip, TooltipContext options) {
		tooltip.add(TextHelper.translatable("tooltip.mtr.railway_sign_" + (isOdd ? "odd" : "even")).formatted(TextFormatting.GRAY));
	}

	public abstract static class BlockEntityBase extends BlockEntityExtension {

		private BlockPos trackPosition = null;
		private static final String KEY_TRACK_FLOOR_POS = "track_floor_pos";

		public BlockEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState state, boolean isOdd) {
			super(type, pos, state);
		}

		@Override
		public void readCompoundTag(CompoundTag compoundTag) {
			final long data = compoundTag.getLong(KEY_TRACK_FLOOR_POS);
			trackPosition = data == 0 ? null : BlockPos.fromLong(data);
			super.readCompoundTag(compoundTag);
		}

		@Override
		public void writeCompoundTag(CompoundTag compoundTag) {
			compoundTag.putLong(KEY_TRACK_FLOOR_POS, trackPosition == null ? 0 : trackPosition.asLong());
		}

		@Override
		public void blockEntityTick() {
			// TODO
		}
	}
}
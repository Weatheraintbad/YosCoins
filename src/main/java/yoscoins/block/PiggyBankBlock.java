package yoscoins.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import yoscoins.init.YosCoinsItems;
import yoscoins.block.entity.PiggyBankBlockEntity;

public class PiggyBankBlock extends BlockWithEntity {

    /* ============= 形状 & 方向 ============= */
    private static final VoxelShape SHAPE = VoxelShapes.cuboid(0.1875, 0.00625, 0.1875,
            0.8125, 0.75, 1.0);
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public PiggyBankBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        Direction face = (player == null ? Direction.NORTH : player.getHorizontalFacing());
        return getDefaultState().with(FACING, face);
    }

    /* ============= 碰撞/渲染形状 ============= */
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world,
                                      BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world,
                                        BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return false;
    }

    /* ============= 存钱 + 潜行旋转 ============= */
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;

        ItemStack held = player.getStackInHand(hand);
        Item item = held.getItem();

        /* 潜行右击 = 旋转 */
        if (player.isSneaking()) {
            if (!world.isClient) {
                BlockState newState = state.cycle(FACING);
                world.setBlockState(pos, newState, Block.NOTIFY_ALL);
                player.sendMessage(Text.literal("已旋转至 " + newState.get(FACING)), true);
            }
            return ActionResult.SUCCESS;
        }

        /* 正常存钱逻辑 */
        CoinType type = null;
        if (item == YosCoinsItems.COPPER_COIN) type = CoinType.COPPER;
        else if (item == YosCoinsItems.SILVER_COIN) type = CoinType.SILVER;
        else if (item == YosCoinsItems.GOLD_COIN) type = CoinType.GOLD;

        if (type != null) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof PiggyBankBlockEntity piggy) {
                int count = held.getCount();
                piggy.deposit(type, count);
                held.decrement(count);
                if (!world.isClient) {
                    String coinName = switch (type) {
                        case COPPER -> "铜";
                        case SILVER -> "银";
                        case GOLD   -> "金";
                    };
                    player.sendMessage(Text.literal("存入了 " + count + " 枚" + coinName + "币"), true);
                }
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof PiggyBankBlockEntity piggy) {
            piggy.dropAllCoins(world, pos);
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PiggyBankBlockEntity(pos, state);
    }

    public enum CoinType {
        COPPER, SILVER, GOLD
    }
}
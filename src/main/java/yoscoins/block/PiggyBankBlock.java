package yoscoins.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
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

import java.util.HashMap;
import java.util.Map;

public class PiggyBankBlock extends BlockWithEntity {

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    // 原始形状（朝北）
    private static final VoxelShape BASE = VoxelShapes.cuboid(
            0.1875, 0.00625, 0.1875,
            0.8125, 0.75,   1.0);

    private static final Map<Direction, VoxelShape> SHAPES = new HashMap<>();

    static {
        SHAPES.put(Direction.NORTH, flipFrontBack(rotateNorthTo(Direction.NORTH)));
        SHAPES.put(Direction.SOUTH, flipFrontBack(rotateNorthTo(Direction.SOUTH)));
        SHAPES.put(Direction.WEST,  flipFrontBack(rotateNorthTo(Direction.WEST)));
        SHAPES.put(Direction.EAST,  flipFrontBack(rotateNorthTo(Direction.EAST)));
    }

    // 把 BASE 转到指定方向
    private static VoxelShape rotateNorthTo(Direction target) {
        return switch (target) {
            case NORTH -> BASE;
            case SOUTH -> rotate180(BASE);
            case WEST  -> rotate270(BASE);
            case EAST  -> rotate90(BASE);
            default    -> BASE;
        };
    }

    // 旋转工具
    private static VoxelShape rotate90(VoxelShape src) {   // EAST
        return src.getBoundingBoxes().stream()
                .map(b -> VoxelShapes.cuboid(
                        b.minZ, b.minY, 1 - b.maxX,
                        b.maxZ, b.maxY, 1 - b.minX))
                .reduce(VoxelShapes.empty(), VoxelShapes::union);
    }
    private static VoxelShape rotate180(VoxelShape src) {
        return src.getBoundingBoxes().stream()
                .map(b -> VoxelShapes.cuboid(
                        1 - b.maxX, b.minY, 1 - b.maxZ,
                        1 - b.minX, b.maxY, 1 - b.minZ))
                .reduce(VoxelShapes.empty(), VoxelShapes::union);
    }
    private static VoxelShape rotate270(VoxelShape src) {  // WEST
        return src.getBoundingBoxes().stream()
                .map(b -> VoxelShapes.cuboid(
                        1 - b.maxZ, b.minY, b.minX,
                        1 - b.minZ, b.maxY, b.maxX))
                .reduce(VoxelShapes.empty(), VoxelShapes::union);
    }

    // 前后翻转
    private static VoxelShape flipFrontBack(VoxelShape src) {
        return src.getBoundingBoxes().stream()
                .map(b -> VoxelShapes.cuboid(
                        b.minX, b.minY, 1 - b.maxZ,
                        b.maxX, b.maxY, 1 - b.minZ))
                .reduce(VoxelShapes.empty(), VoxelShapes::union);
    }

    public PiggyBankBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    // 模型正面朝向玩家
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        PlayerEntity pl = ctx.getPlayer();
        Direction face = (pl == null ? Direction.NORTH : pl.getHorizontalFacing());
        return getDefaultState().with(FACING, face);
    }

    // 碰撞箱
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world,
                                      BlockPos pos, ShapeContext context) {
        return SHAPES.get(state.get(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world,
                                        BlockPos pos, ShapeContext context) {
        return SHAPES.get(state.get(FACING));
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return false;
    }

    // 存钱功能
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;

        ItemStack held = player.getStackInHand(hand);
        CoinType type = null;
        if (held.isOf(YosCoinsItems.COPPER_COIN)) type = CoinType.COPPER;
        else if (held.isOf(YosCoinsItems.SILVER_COIN)) type = CoinType.SILVER;
        else if (held.isOf(YosCoinsItems.GOLD_COIN)) type = CoinType.GOLD;

        if (type != null) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof PiggyBankBlockEntity piggy) {
                int count = held.getCount();
                piggy.deposit(type, count);
                held.decrement(count);
                String coinName = switch (type) {
                    case COPPER -> "铜";
                    case SILVER -> "银";
                    case GOLD   -> "金";
                };
                player.sendMessage(Text.literal("存入了 " + count + " 枚" + coinName + "币"), true);
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

    public enum CoinType { COPPER, SILVER, GOLD }
}
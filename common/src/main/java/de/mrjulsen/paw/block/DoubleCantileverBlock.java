package de.mrjulsen.paw.block;

import java.util.function.Function;

import de.mrjulsen.paw.block.property.EInsulatorType;
import de.mrjulsen.paw.util.Const;
import de.mrjulsen.paw.util.ModMath;
import de.mrjulsen.mcdragonlib.util.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DoubleCantileverBlock extends CantileverBlock {

    private static final String NBT_ITEM_DATA_CLICKED_SIDE = "ClickedRight";

    private final Function<Direction, Integer> directionIndexGetter = (dir) -> {
        return switch (dir) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
    };

    public DoubleCantileverBlock(Properties properties, byte size, EInsulatorType insulatorType) {
        super(properties, size, insulatorType);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return false;
    }

    @Override
    public void onPlaceWireOn(Level level, BlockPos pos, BlockState state, Player player, UseOnContext hit, CompoundTag itemData, int index) {
        /*
         * Places the cable depending on the position clicked.
         */
        Direction targetDir = hit.getClickedFace();
        Direction facing = state.getValue(FACING);
        Vec2[] points = getCubeCorners(state, level, pos, null);

        if (targetDir.getAxis() == Axis.Y) {
            Vec3 clicked = hit.getClickLocation().subtract(MathUtils.blockPosToVec3(pos));

            int i = directionIndexGetter.apply(facing);
            Vec2 pointA = points[i];
            Vec2 pointB = points[(i + 1) % points.length];

            float minX = Math.min(pointA.x, pointB.x);
            float minZ = Math.min(pointA.y, pointB.y);
            float maxX = Math.max(pointA.x, pointB.x);
            float maxZ = Math.max(pointA.y, pointB.y);

            Vec2 point1 = new Vec2((minX + maxX) / 2, (minZ + maxZ) / 2);

            
            i = directionIndexGetter.apply(facing.getOpposite());
            pointA = points[i];
            pointB = points[(i + 1) % points.length];

            minX = Math.min(pointA.x, pointB.x);
            minZ = Math.min(pointA.y, pointB.y);
            maxX = Math.max(pointA.x, pointB.x);
            maxZ = Math.max(pointA.y, pointB.y);

            Vec2 point2 = new Vec2((minX + maxX) / 2, (minZ + maxZ) / 2);

            itemData.putBoolean(NBT_ITEM_DATA_CLICKED_SIDE + index, ModMath.checkPointPosition(point1, point2, new Vec2((float)clicked.x, (float)clicked.z)) > 0);
        } else if (targetDir.getAxis() == facing.getAxis()) {
            int i = directionIndexGetter.apply(targetDir);
            Vec2 pointA = points[i];
            Vec2 pointB = points[(i + 1) % points.length];

            float minX = Math.min(pointA.x, pointB.x);
            float minZ = Math.min(pointA.y, pointB.y);
            float maxX = Math.max(pointA.x, pointB.x);
            float maxZ = Math.max(pointA.y, pointB.y);
            float diffX = Math.abs(maxX - minX);

            Vec3 clicked = hit.getClickLocation().subtract(MathUtils.blockPosToVec3(pos));
            if (clicked.x >= minX && clicked.x <= maxX && clicked.z >= minZ && clicked.z <= maxZ) {
                itemData.putBoolean(NBT_ITEM_DATA_CLICKED_SIDE + index, clicked.x < minX + diffX / 2);
            }
        } else if (targetDir.getAxis() == facing.getClockWise().getAxis()) {
            if (facing.getCounterClockWise() == targetDir) {
                itemData.putBoolean(NBT_ITEM_DATA_CLICKED_SIDE + index, true);
            } else if (facing.getClockWise() == targetDir) {
                itemData.putBoolean(NBT_ITEM_DATA_CLICKED_SIDE + index, false);
            }
        }
    }

    @Override
    public VoxelShape getBaseShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public Vec3 defaultWireAttachPoint(Level level, BlockPos pos, BlockState state, CompoundTag itemData, boolean firstPoint) {
        Vec3 base = super.defaultWireAttachPoint(level, pos, state, itemData, firstPoint);

        boolean side = itemData.getBoolean(firstPoint ? NBT_ITEM_DATA_CLICKED_SIDE + "1" : NBT_ITEM_DATA_CLICKED_SIDE + "2");
        boolean outside = state.getValue(REGISTRATION_ARM) == ECantileverRegistrationArmType.OUTER;
        boolean centered = state.getValue(REGISTRATION_ARM) == ECantileverRegistrationArmType.CENTER;

        float x = Const.PIXEL * (side ? -9 : 9);
        float y = Const.PIXEL * (side && !centered ? 8 : 0);
        float z = 0;

        if (centered) {
            z = side ? -4 : 4;
        } else if (side) {
            z = outside ? 12.5f : -11.5f;
        }
        z *= Const.PIXEL;
        
        return base.add(x, y, z);

    }

    @Override
    public Vec3 tensionWireAttachPoint(Level level, BlockPos pos, BlockState state, CompoundTag itemData, boolean firstPoint) {
        Vec3 base = super.tensionWireAttachPoint(level, pos, state, itemData, firstPoint);
        
        boolean side = itemData.getBoolean(firstPoint ? NBT_ITEM_DATA_CLICKED_SIDE + "1" : NBT_ITEM_DATA_CLICKED_SIDE + "2");
        boolean armOutside = state.getValue(REGISTRATION_ARM) == ECantileverRegistrationArmType.OUTER;
        
        return side ? 
            base.add(Const.PIXEL * -9, Const.PIXEL * (armOutside ? 7 : 0), Const.PIXEL * (armOutside ? 8 : -8)) :
            base.add(Const.PIXEL * 9 , Const.PIXEL * (armOutside ? 0 : 7), 0);
    }

    @Override
    public Vec3 multiblockSize() {
        return new Vec3(1, 1, 1);
    }
}

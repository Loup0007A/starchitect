package com.starchitect.client;

import com.starchitect.analysis.Grid;
import com.starchitect.analysis.Kind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Random;

/** Relève les blocs autour du joueur, colonne par colonne (une partie à chaque tick). */
public final class Scanner {

    private final Level level;
    public final Grid grid;
    /** Position du joueur dans la grille. */
    public final int cx, cz;
    private int column;
    private final Map<BlockState, byte[]> cache = new IdentityHashMap<>();
    private final Map<BlockState, Short> ids = new IdentityHashMap<>();
    private final Random rnd = new Random();

    public Scanner(Level level, BlockPos center, int radius, int below, int above) {
        this.level = level;
        int minY = Math.max(level.getMinBuildHeight(), center.getY() - below);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, center.getY() + above);
        grid = new Grid(center.getX() - radius, minY, center.getZ() - radius, radius * 2 + 1, maxY - minY + 1, radius * 2 + 1);
        cx = radius;
        cz = radius;
    }

    public boolean done() {
        return column >= grid.sx * grid.sz;
    }

    public float progress() {
        return Math.min(1f, column / (float) (grid.sx * grid.sz));
    }

    /**
     * Relève quelques colonnes.
     * @param sparks nombre maximal d'étincelles à faire apparaître sur les blocs construits relevés
     */
    public void step(int columns, int sparks) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int total = grid.sx * grid.sz;
        int end = Math.min(total, column + columns);
        int seen = 0;
        for (; column < end; column++) {
            int x = column % grid.sx, z = column / grid.sx;
            for (int y = 0; y < grid.sy; y++) {
                p.set(grid.ox + x, grid.oy + y, grid.oz + z);
                BlockState st = level.getBlockState(p);
                byte k = kind(st);
                int i = grid.at(x, y, z);
                grid.kind[i] = k;
                grid.id[i] = id(st);
                if (k != Kind.NATURAL && k != Kind.DIRT && k != Kind.LOG) {
                    grid.light[i] = (byte) level.getBrightness(LightLayer.BLOCK, p);
                }
                if (sparks > 0 && Kind.built(k) && k != Kind.LIGHT) {
                    seen++;
                    if (rnd.nextInt(seen) < sparks && level.getBlockState(p.above()).isAir()) {
                        level.addParticle(ParticleTypes.END_ROD, p.getX() + 0.5, p.getY() + 1.05, p.getZ() + 0.5,
                            0, 0.02 + rnd.nextFloat() * 0.03, 0);
                        sparks--;
                    }
                }
            }
        }
        if (done()) things();
    }

    private short id(BlockState st) {
        Short s = ids.get(st);
        if (s != null) return s;
        short v = grid.idOf(BuiltInRegistries.BLOCK.getKey(st.getBlock()).toString());
        ids.put(st, v);
        return v;
    }

    private byte kind(BlockState st) {
        byte[] c = cache.get(st);
        if (c != null) return c[0];
        String path = BuiltInRegistries.BLOCK.getKey(st.getBlock()).getPath();
        boolean persistent = st.hasProperty(BlockStateProperties.PERSISTENT) && st.getValue(BlockStateProperties.PERSISTENT);
        char axis = 0;
        if (st.hasProperty(BlockStateProperties.AXIS)) {
            Direction.Axis a = st.getValue(BlockStateProperties.AXIS);
            axis = a == Direction.Axis.X ? 'x' : a == Direction.Axis.Z ? 'z' : 'y';
        }
        byte k = st.isAir() ? Kind.AIR : Kind.classify(path, persistent, axis, st.getLightEmission());
        cache.put(st, new byte[]{k});
        return k;
    }

    private void things() {
        AABB box = new AABB(grid.ox, grid.oy, grid.oz, grid.ox + grid.sx, grid.oy + grid.sy, grid.oz + grid.sz);
        for (Entity e : level.getEntities((Entity) null, box, e -> true)) {
            String type = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath();
            boolean tame = e instanceof TamableAnimal t && t.isTame() || e instanceof AbstractHorse h && h.isTamed();
            BlockPos b = e.blockPosition();
            grid.things.add(new Grid.Thing(type, b.getX(), b.getY(), b.getZ(), tame));
        }
    }
}

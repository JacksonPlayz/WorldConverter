package me.dags.converter.data.tile.chisel;

import me.dags.converter.block.BlockState;
import me.dags.converter.registry.RemappingRegistry;
import org.jnbt.CompoundTag;
import org.jnbt.Nbt;

public class ChiselTileLegacy implements ChiselTile.Reader, ChiselTile.Writer {

    public static final String ID_PREFIX = "mod.chiselandbits";

    private final CompoundTag root;
    private final RemappingRegistry<BlockState> registry;

    public ChiselTileLegacy(CompoundTag root, RemappingRegistry<BlockState> registry) {
        this.root = root;
        this.registry = registry;
    }

    public ChiselTileLegacy(RemappingRegistry<BlockState> registry) {
        this(Nbt.compound(), registry);
    }

    @Override
    public boolean isValid() {
        return getTileId().startsWith(ID_PREFIX) && root.get("X").isPresent();
    }

    @Override
    public String getTileId() {
        return root.getString("id");
    }

    @Override
    public BlockState[] getStateArray() {
        int[] stateIds = root.getInts("X");
        BlockState[] states = new BlockState[stateIds.length];
        for (int i = 0; i < stateIds.length; i++) {
            states[i] = registry.getInput(stateIds[i]);
        }
        return states;
    }

    @Override
    public void setTileId(String id) {
        root.put("id", id);
    }

    @Override
    public void setStateArray(BlockState[] states) {
        int[] stateIds = new int[states.length];
        for (int i = 0; i < stateIds.length; i++) {
            stateIds[i] = states[i].getId();
        }
        root.put("X", stateIds);
    }

    @Override
    public CompoundTag getRoot() {
        return root;
    }
}

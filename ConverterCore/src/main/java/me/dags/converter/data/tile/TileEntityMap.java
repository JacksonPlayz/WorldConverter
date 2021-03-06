package me.dags.converter.data.tile;

import org.jetbrains.annotations.Nullable;
import org.jnbt.CompoundTag;

public interface TileEntityMap {

    @Nullable
    CompoundTag getEntity(int x, int y, int z);
}

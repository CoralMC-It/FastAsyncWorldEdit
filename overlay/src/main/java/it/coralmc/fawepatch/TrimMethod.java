package it.coralmc.fawepatch;

import com.fastasyncworldedit.bukkit.adapter.AbstractBukkitGetBlocks;
import com.sk89q.worldedit.bukkit.adapter.impl.fawe.v26_2.PaperweightPlatformAdapter;
import java.util.concurrent.locks.ReadWriteLock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.HashMapPalette;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LinearPalette;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PalettedContainer;

/** Compile-time method donor; never shipped or instantiated. Existing fields/methods are ABI anchors. */
public abstract class TrimMethod extends AbstractBukkitGetBlocks<ServerLevel, LevelChunk> {
    private ReadWriteLock sectionLock;
    private LevelChunkSection[] sections;
    private LevelChunk levelChunk;
    private DataLayer[] skyLight;
    private DataLayer[] blockLight;
    protected TrimMethod(ServerLevel world, int x, int z, int min, int max) { super(world, x, z, min, max); }
    public abstract LevelChunkSection[] getSections(boolean force);
    @Override public abstract boolean hasSection(int layer);

    @Override
    @SuppressWarnings("unchecked")
    public boolean trim(boolean aggressive) {
        // The actual source fix is PaperweightGetBlocks.trim in adapter-26.2.
        sectionLock.writeLock().lock();
        try {
            if (aggressive || sections == null) {
                synchronized (this) {
                    skyLight = new DataLayer[getSectionCount()];
                    blockLight = new DataLayer[getSectionCount()];
                    if (!aggressive) return true;
                    sections = null;
                    levelChunk = null;
                }
            }
        } finally {
            sectionLock.writeLock().unlock();
        }
        if (aggressive) return super.trim(true);
        LevelChunkSection[] snapshot = getSections(true);
        for (int i = getMinSectionPosition(); i <= getMaxSectionPosition(); i++) {
            int layer = i - getMinSectionPosition();
            synchronized (super.sectionLocks[layer]) {
                if (super.blocks[layer] == null) continue;
                LevelChunkSection existing = snapshot[layer];
                if (existing == null) { super.trim(false, i); continue; }
                try {
                    final PalettedContainer<BlockState> blocksExisting = existing.getStates();
                    final Object dataObject = PaperweightPlatformAdapter.fieldData.get(blocksExisting);
                    final Palette<BlockState> palette = (Palette<BlockState>) PaperweightPlatformAdapter.fieldPalette.get(dataObject);
                    if ((palette instanceof LinearPalette || palette instanceof HashMapPalette) && palette.getSize() == 1) continue;
                    super.trim(false, i);
                } catch (IllegalAccessException ignored) {
                    super.trim(false, i);
                }
            }
        }
        return true;
    }
}

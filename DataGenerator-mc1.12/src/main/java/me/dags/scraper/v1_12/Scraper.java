package me.dags.scraper.v1_12;

import me.dags.converter.datagen.GameDataWriter;
import me.dags.converter.datagen.Schema;
import me.dags.converter.datagen.SectionWriter;
import me.dags.converter.datagen.biome.BiomeData;
import me.dags.converter.datagen.block.BlockData;
import me.dags.converter.datagen.block.StateData;
import me.dags.converter.version.versions.MinecraftVersion;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@Mod(modid = "data_generator")
public class Scraper {

    public Scraper() {
        MinecraftForge.EVENT_BUS.register(Scraper.class);
        MinecraftForge.EVENT_BUS.register(ClientScraper.class);
    }

    @Mod.EventHandler
    public static void post(FMLPostInitializationEvent event) {
        if (event.getSide() == Side.CLIENT) {
            ClientScraper.dump();
        }
    }

    @SubscribeEvent
    public static void load(WorldEvent.Load event) {
        File dir = event.getWorld().getSaveHandler().getWorldDirectory();
        File out = new File(dir, "game_data.json");
        Schema schema = Schema.legacy("1.12");
        try (GameDataWriter writer = new GameDataWriter(schema, out)) {
            try (SectionWriter<BlockData> section = writer.startBlocks()) {
                for (Block block : ForgeRegistries.BLOCKS) {
                    section.write(getBlockData(block));
                }
            }
            try (SectionWriter<BiomeData> section = writer.startBiomes()) {
                for (Biome biome : ForgeRegistries.BIOMES) {
                    section.write(geBiomeData(biome));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Mappings.generate();
    }

    private static BiomeData geBiomeData(Biome biome) {
        return new BiomeData(biome.getRegistryName(), Biome.getIdForBiome(biome));
    }

    private static BlockData getBlockData(Block block) {
        Object name = block.getRegistryName();
        int blockId = Block.getIdFromBlock(block);
        boolean upgrade = hasTransientProperties(block);
        StateData defaults = getStateData(block.getDefaultState());
        List<StateData> states = new LinkedList<>();
        for (IBlockState state : block.getBlockState().getValidStates()) {
            states.add(getStateData(state));
        }
        return new BlockData(name, blockId, upgrade, defaults, states);
    }

    private static StateData getStateData(IBlockState state) {
        return new StateData(propertyString(state), state.getBlock().getMetaFromState(state));
    }

    private static String propertyString(IBlockState state) {
        StringBuilder sb = new StringBuilder();
        state.getPropertyKeys().stream().sorted(Comparator.comparing(IProperty::getName)).forEach(p -> {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(p.getName()).append('=').append(state.getValue(p).toString().toLowerCase());
        });
        return sb.toString();
    }

    private static boolean hasTransientProperties(Block block) {
        if (block.getBlockState().getProperty("snowy") != null) {
            return false;
        }
        int[] metas = new int[16];
        for (IBlockState state : block.getBlockState().getValidStates()) {
            int meta = block.getMetaFromState(state);
            metas[meta]++;
        }
        for (int i : metas) {
            if (i > 1) {
                return true;
            }
        }
        return false;
    }
}

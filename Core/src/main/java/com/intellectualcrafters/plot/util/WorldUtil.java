package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.schematic.PlotItem;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public abstract class WorldUtil {
    public static WorldUtil IMP;
    
    public abstract int getBiomeFromString(String value);
    
    public abstract String[] getBiomeList();
    
    public abstract String getMainWorld();

    public abstract boolean isWorld(String worldname);
    
    public abstract String[] getSign(Location loc);
    
    public abstract Location getSpawn(String world);

    public abstract void setSpawn(Location loc);

    public abstract void saveWorld(String world);
    
    public abstract String getClosestMatchingName(PlotBlock plotBlock);
    
    public abstract boolean isBlockSolid(PlotBlock block);
    
    public abstract StringComparison<PlotBlock>.ComparisonResult getClosestBlock(String name);
    
    public abstract String getBiome(String world, int x, int z);
    
    public abstract PlotBlock getBlock(Location location);
    
    public abstract int getHighestBlock(String world, int x, int z);
    
    public abstract boolean addItems(String world, PlotItem item);
    
    public abstract void setSign(String world, int x, int y, int z, String[] lines);
    
    public abstract void setBiomes(String world, RegionWrapper region, String biome);

    public void upload(final Plot plot, final UUID uuid, final String file, final RunnableVal<URL> whenDone) {
        if (plot == null) {
            throw new IllegalArgumentException("Plot may not be null!");
        }
        final Location home = plot.getHome();
        MainUtil.upload(uuid, file, "zip", new RunnableVal<OutputStream>() {
            @Override
            public void run(OutputStream output) {
                try (final ZipOutputStream zos = new ZipOutputStream(output)) {
                    final byte[] buffer = new byte[1024];
                    final File dat = getDat(plot.getArea().worldname);
                    Location spawn = getSpawn(plot.getArea().worldname);
                    setSpawn(home);
                    if (dat != null) {
                        final ZipEntry ze = new ZipEntry("world" + File.separator + dat.getName());
                        zos.putNextEntry(ze);
                        final FileInputStream in = new FileInputStream(dat);
                        int len;
                        while ((len = in.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                    }
                    setSpawn(spawn);
                    for (Plot current : plot.getConnectedPlots()) {
                        final Location bot = current.getBottomAbs();
                        final Location top = current.getTopAbs();
                        final int brx = bot.getX() >> 9;
                        final int brz = bot.getZ() >> 9;
                        final int trx = top.getX() >> 9;
                        final int trz = top.getZ() >> 9;
                        for (int x = brx; x <= trx; x++) {
                            for (int z = brz; z <= trz; z++) {
                                final File file = getMcr(plot.getArea().worldname, x, z);
                                if (file != null) {
                                    //final String name = "r." + (x - cx) + "." + (z - cz) + ".mca";
                                    String name = file.getName();
                                    final ZipEntry ze = new ZipEntry("world" + File.separator + "region" + File.separator + name);
                                    zos.putNextEntry(ze);
                                    final FileInputStream in = new FileInputStream(file);
                                    int len;
                                    while ((len = in.read(buffer)) > 0) {
                                        zos.write(buffer, 0, len);
                                    }
                                    in.close();
                                }
                            }
                        }
                    }
                    zos.closeEntry();
                    zos.flush();
                    zos.finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, whenDone);
    }

    public File getDat(final String world) {
        final File file = new File(PS.get().IMP.getWorldContainer() + File.separator + world + File.separator + "level.dat");
        if (file.exists()) {
            return file;
        }
        return null;
    }

    public File getMcr(final String world, final int x, final int z) {
        final File file = new File(PS.get().IMP.getWorldContainer(), world + File.separator + "region" + File.separator + "r." + x + "." + z + ".mca");
        if (file.exists()) {
            return file;
        }
        return null;
    }
}
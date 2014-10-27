package de.unisaarland.UniApp.campus.model;

import android.content.res.AssetManager;
import android.graphics.Rect;
import android.util.SparseArray;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Janek on 25.09.2014.
 */
public class CustomMapHomProvider implements TileProvider {
    private static final int TILE_WIDTH = 256;
    private static final int TILE_HEIGHT = 256;
    private static final int BUFFER_SIZE = 16 * 1024;

    private AssetManager mAssets;

    public CustomMapHomProvider(AssetManager assets) {
        mAssets = assets;
    }

    private static final SparseArray<Rect> TILE_ZOOMS = new SparseArray<Rect>() {{
        put(14,  new Rect(8525,  5604,  8526,  5605 ));
        put(15,  new Rect(17051,  11209,  17053,  11210 ));
        put(16, new Rect(34103,  22419,  34106,  22421 ));
        put(17, new Rect(68207, 44839, 68212, 44842));

    }};

    private boolean hasTile(int x, int y, int zoom) {
        Rect b = TILE_ZOOMS.get(zoom);
        return b == null ? false : (b.left <= x && x <= b.right && b.top <= y && y <= b.bottom);
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        y = fixYCoordinate(y, zoom);
    //    if(hasTile(x,y,zoom)){
            byte[] image = readTileImage(x, y, zoom);
            if (image != null)
                return new Tile(TILE_WIDTH, TILE_HEIGHT, image);
            else
                return null;
     //   }
    //    return NO_TILE;
    }

    private byte[] readTileImage(int x, int y, int zoom) {
        InputStream in = null;
        ByteArrayOutputStream buffer = null;

        try {
            in = mAssets.open(getTileFilename(x, y, zoom));
            buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[BUFFER_SIZE];

            while ((nRead = in.read(data, 0, BUFFER_SIZE)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();

            return buffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) try { in.close(); } catch (Exception ignored) {}
            if (buffer != null) try { buffer.close(); } catch (Exception ignored) {}
        }
    }

    private String getTileFilename(int x, int y, int zoom) {
        return "tiles/hom/OverlayTiles/" + zoom + '/' + x + '/' + y + ".png";
    }

    /**
     * Fixing tile's y index (reversing order)
     */
    private int fixYCoordinate(int y, int zoom) {
        int size = 1 << zoom; // size = 2^zoom
        return size - 1 - y;
    }
}


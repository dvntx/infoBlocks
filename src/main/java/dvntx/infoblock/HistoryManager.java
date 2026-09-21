package dvntx.infoblock;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores the history for each block position in memory (and disk).
 * The internal key format is "level;x;y;z".
 */
public class HistoryManager {

    private final File file;
    private final int maxEntriesPerBlock;
    private Map<String, LinkedList<HistoryEntry>> data = new ConcurrentHashMap<>();
    private volatile boolean dirty = false;

    public HistoryManager(File file, int maxEntriesPerBlock) {
        this.file = file;
        this.maxEntriesPerBlock = Math.max(1, maxEntriesPerBlock);
    }

    private static String key(String level, int x, int y, int z) {
        return level + ";" + x + ";" + y + ";" + z;
    }

    public void addEntry(String level, int x, int y, int z, String action, String playerName, String blockName) {
        String k = key(level, x, y, z);
        LinkedList<HistoryEntry> list = data.computeIfAbsent(k, kk -> new LinkedList<>());
        synchronized (list) {
            list.addLast(new HistoryEntry(playerName, action, blockName, System.currentTimeMillis()));
            while (list.size() > maxEntriesPerBlock) {
                list.removeFirst();
            }
        }
        dirty = true;
    }

    /**
     * Returns a copy of the history for that position (never null).
     */
    public List<HistoryEntry> getEntries(String level, int x, int y, int z) {
        LinkedList<HistoryEntry> list = data.get(key(level, x, y, z));
        if (list == null) {
            return List.of();
        }
        synchronized (list) {
            return new LinkedList<>(list);
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void load() {
        if (!file.exists()) {
            return;
        }
        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            Object obj = in.readObject();
            if (obj instanceof Map) {
                data = (Map<String, LinkedList<HistoryEntry>>) obj;
            }
        } catch (Exception e) {
            Logger.getLogger("InfoBlock").log(Level.WARNING, "Could not load InfoBlock history", e);
        }
    }

    public synchronized void save() {
        if (!dirty) {
            return;
        }
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            out.writeObject(data);
            dirty = false;
        } catch (IOException e) {
            Logger.getLogger("InfoBlock").log(Level.WARNING, "Could not save InfoBlock history", e);
        }
    }
}
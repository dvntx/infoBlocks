package dvntx.infoblock;

import org.powernukkitx.Player;
import org.powernukkitx.block.Block;
import org.powernukkitx.blockentity.BlockEntity;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.EventPriority;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.block.BlockBreakEvent;
import org.powernukkitx.event.block.BlockPlaceEvent;
import org.powernukkitx.event.inventory.InventoryOpenEvent;
import org.powernukkitx.event.player.PlayerInteractEvent;
import org.powernukkitx.event.player.PlayerQuitEvent;
import org.powernukkitx.inventory.Inventory;
import org.powernukkitx.inventory.InventoryHolder;
import org.powernukkitx.level.Level;
import org.powernukkitx.plugin.PluginBase;
import org.powernukkitx.utils.TextFormat;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class InfoBlocks extends PluginBase implements Listener {

    private HistoryManager historyManager;
    private final Set<UUID> inspecting = new HashSet<>();

    private int maxLinesShown = 10;
    private boolean logContainerOpens = true;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        int maxEntriesPerBlock = getConfig().getInt("max-history-per-block", 50);
        int autosaveMinutes = getConfig().getInt("autosave-minutes", 5);
        this.maxLinesShown = getConfig().getInt("max-lines-shown", 10);
        this.logContainerOpens = getConfig().getBoolean("log-container-opens", true);

        this.historyManager = new HistoryManager(new File(getDataFolder(), "history.dat"), maxEntriesPerBlock);
        this.historyManager.load();

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

        // Register command
        String prefix = getName().toLowerCase(java.util.Locale.ROOT);
        getServer().getCommandMap().register(prefix, new InfoBlockCommand(this));

        if (autosaveMinutes > 0) {
            int ticks = autosaveMinutes * 60 * 20;
            getServer().getScheduler().scheduleDelayedRepeatingTask(this, () -> historyManager.save(), ticks, ticks);
        }

        getLogger().info(TextFormat.GREEN + "InfoBlock enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (historyManager != null) {
            historyManager.save();
        }
        inspecting.clear();
    }

    public boolean toggleInspector(Player player) {
        UUID id = player.getUniqueId();
        if (inspecting.contains(id)) {
            inspecting.remove(id);
            return false;
        }
        inspecting.add(id);
        return true;
    }

    public boolean isInspecting(Player player) {
        return inspecting.contains(player.getUniqueId());
    }

    // ---------------------------------------------------------------
    // Inspector mode: clicking a block/chest displays its history
    // ---------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isInspecting(player)) {
            return;
        }

        PlayerInteractEvent.Action action = event.getAction();
        if (action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK
                && action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            return;
        }

        event.setCancelled(true);

        Block block = event.getBlock();
        if (block == null || block.getLevel() == null) {
            return;
        }

        int x = (int) Math.floor(block.getX());
        int y = (int) Math.floor(block.getY());
        int z = (int) Math.floor(block.getZ());
        String levelName = block.getLevel().getName();

        showHistory(player, levelName, x, y, z, block.getName());
    }

    // ---------------------------------------------------------------
    // Background logging: place / break / open containers
    // ---------------------------------------------------------------

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (isInspecting(player)) {
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlock();
        if (block == null || block.getLevel() == null) {
            return;
        }

        int x = (int) Math.floor(block.getX());
        int y = (int) Math.floor(block.getY());
        int z = (int) Math.floor(block.getZ());

        historyManager.addEntry(block.getLevel().getName(), x, y, z, "Placed", player.getName(), block.getName());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (isInspecting(player)) {
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlock();
        if (block == null || block.getLevel() == null) {
            return;
        }

        int x = (int) Math.floor(block.getX());
        int y = (int) Math.floor(block.getY());
        int z = (int) Math.floor(block.getZ());

        historyManager.addEntry(block.getLevel().getName(), x, y, z, "Broken", player.getName(), block.getName());
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!logContainerOpens) {
            return;
        }

        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();

        // We only care about inventories belonging to a world block (chests, furnaces, barrels, etc.)
        if (!(holder instanceof BlockEntity)) {
            return;
        }

        BlockEntity blockEntity = (BlockEntity) holder;
        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }

        int x = (int) Math.floor(blockEntity.getX());
        int y = (int) Math.floor(blockEntity.getY());
        int z = (int) Math.floor(blockEntity.getZ());

        Player player = event.getPlayer();
        historyManager.addEntry(level.getName(), x, y, z, "Opened", player.getName(), blockEntity.getName());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        inspecting.remove(event.getPlayer().getUniqueId());
    }

    // ---------------------------------------------------------------
    // Utilities
    // ---------------------------------------------------------------

    private void showHistory(Player player, String levelName, int x, int y, int z, String blockName) {
        List<HistoryEntry> entries = historyManager.getEntries(levelName, x, y, z);

        player.sendMessage(TextFormat.GOLD + "" + TextFormat.BOLD + "----- History: " + blockName + " -----");
        player.sendMessage(TextFormat.GRAY + "Position: " + levelName + " (" + x + ", " + y + ", " + z + ")");

        if (entries.isEmpty()) {
            player.sendMessage(TextFormat.YELLOW + "No history records found for this block yet.");
            return;
        }

        int start = Math.max(0, entries.size() - maxLinesShown);
        for (int i = start; i < entries.size(); i++) {
            player.sendMessage(TextFormat.AQUA + entries.get(i).format());
        }

        if (entries.size() > maxLinesShown) {
            player.sendMessage(TextFormat.GRAY + "Showing the last " + maxLinesShown + " out of " + entries.size() + " records.");
        }
    }
}

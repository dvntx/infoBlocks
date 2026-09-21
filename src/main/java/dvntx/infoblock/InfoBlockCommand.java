package dvntx.infoblock;

import org.powernukkitx.Player;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandSender;
import org.powernukkitx.utils.TextFormat;

public class InfoBlockCommand extends Command {

    private final InfoBlocks plugin;

    public InfoBlockCommand(InfoBlocks plugin) {
        super("infoblock", "Toggles the InfoBlock inspector mode", "/infoblock",
                new String[]{"infoblocks", "ib"});
        this.plugin = plugin;
        this.setPermission("infoblock.use");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(TextFormat.RED + "This command can only be used in-game.");
            return true;
        }

        if (!sender.hasPermission("infoblock.use")) {
            sender.sendMessage(TextFormat.RED + "You do not have permission to use this command.");
            return true;
        }

        Player player = (Player) sender;
        boolean nowInspecting = plugin.toggleInspector(player);

        if (nowInspecting) {
            player.sendMessage(TextFormat.GREEN + "Inspector mode enabled.");
            player.sendMessage(TextFormat.GRAY + "Click on any block or chest to check its history.");
            player.sendMessage(TextFormat.GRAY + "Type /infoblock again to exit inspector mode.");
        } else {
            player.sendMessage(TextFormat.YELLOW + "Inspector mode disabled.");
        }
        return true;
    }
}
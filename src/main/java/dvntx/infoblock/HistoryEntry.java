package dvntx.infoblock;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;


public class HistoryEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String playerName;
    private final String action;
    private final String blockName;
    private final long timestamp;

    public HistoryEntry(String playerName, String action, String blockName, long timestamp) {
        this.playerName = playerName;
        this.action = action;
        this.blockName = blockName;
        this.timestamp = timestamp;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getAction() {
        return action;
    }

    public String getBlockName() {
        return blockName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Formats the entry to display it in the chat, e.g.:
     * [20/09/2026 14:32:10] Steve -> Placed (Stone) or Steve -> Opened (Chest)
     */
    public String format() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return "[" + sdf.format(new Date(timestamp)) + "] " + playerName + " -> " + action + " (" + blockName + ")";
    }
}
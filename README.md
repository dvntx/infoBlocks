# infoBlocks
📦 InfoBlock
 (PowerNukkitX Plugin)
InfoBlock is a lightweight and efficient plugin designed for PowerNukkitX servers that tracks and maintains a detailed history of blocks throughout your world, recording exactly who interacted with them, what action was performed, and when. 
It is an essential tool for server administrators and moderators to quickly investigate griefing, theft, or block changes directly in-game.  

✨ Key Features

 🔍 Interactive Inspector Mode: Toggles an inspection mode that allows staff to view the history of any block or container with a simple left or right click.  
 
📝 Background Event Tracking: Logs essential world actions automatically in real time:
 Block placements.  
 Block breaking.  
 Container openings (chests, furnaces, barrels, etc.).  
 
📅 Detailed History Logs: Displays formatted entries in chat showing timestamp, player name, action performed, and block name.  
 
💾 Persistent Disk Storage: Built-in ⁠HistoryManager⁠ saves and loads data safely with configurable periodic auto-saving.  
 
🛡️ Clean Session Handling: Clears inspection states automatically upon player disconnect to prevent stuck modes.  

🎮 Commands & Permissions
 
Command: ⁠/infoblock⁠ (Aliases: ⁠/infoblocks⁠, ⁠/ib⁠)  

Permission: ⁠infoblock.use⁠ (Required to run the command and use the inspector mode)  

⚙️ Configuration (⁠config.yml⁠)

The plugin features a flexible config file to adjust limits and behavior:

 ⁠max-history-per-block⁠: Maximum history entries retained per block position.  

 ⁠autosave-minutes⁠: Interval in minutes for auto-saving history to disk.  

 ⁠max-lines-shown⁠: Maximum number of recent history lines displayed in chat per inspection.  

 ⁠log-container-opens⁠: Enable or disable logging when players open container inventories.

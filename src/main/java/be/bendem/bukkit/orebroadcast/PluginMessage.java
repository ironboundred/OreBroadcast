package be.bendem.bukkit.orebroadcast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class PluginMessage implements PluginMessageListener {
	
    private final OreBroadcast plugin;

    public PluginMessage(OreBroadcast plugin) {
        this.plugin = plugin;
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", this);
    }

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
	    if (!channel.equals("BungeeCord")) {
	        return;
	      }
	    
	     ByteArrayDataInput in = ByteStreams.newDataInput(message);
    	 String subchannel = in.readUTF();
    	 
    	 if (subchannel.equals("orebroadcast")) {
    		 String data = "";
	       	 short len = in.readShort();
	       	 byte[] msgbytes = new byte[len];
	       	 in.readFully(msgbytes);
	
	       	 DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
	       	 
	       	 try {
	       		 data = msgin.readUTF();
	       	 } catch (IOException e) {
	   			// TODO Auto-generated catch block
	   			e.printStackTrace();
	       	 } 
	       	 
	       	 // Get recipients
	         for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
	              if(onlinePlayer.hasPermission("ob.receive") && !plugin.isOptOut(onlinePlayer)) {
	                  onlinePlayer.sendMessage(data);
	              }
	         }
    	}
	}
	
	public void sendPluginMessage(String prefix, String msg) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Forward"); // So BungeeCord knows to forward it
		out.writeUTF("ALL");
		out.writeUTF("orebroadcast"); // The channel name to check if this your data

		ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
		DataOutputStream msgout = new DataOutputStream(msgbytes);
		try {
			String message = prefix + " " + msg;
			
			msgout.writeUTF(message); // You can do anything you want with msgout
		} catch (IOException exception){
			exception.printStackTrace();
		}
		
		out.writeShort(msgbytes.toByteArray().length);
		out.write(msgbytes.toByteArray());
		
		Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
	}
	
}

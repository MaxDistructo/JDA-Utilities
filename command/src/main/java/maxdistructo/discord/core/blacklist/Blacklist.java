package maxdistructo.discord.core.blacklist;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class Blacklist {
    private static Long[] load() {
        JSONObject json = null;
        ArrayList<Long> ret = new ArrayList<Long>();
        json = readJSONFromFile("/config/blacklist");
        if(json != null) {
            JSONObject tempBan = json.getJSONObject("temp_ban");
            JSONArray permBan = json.getJSONArray("perm_ban");

            for (Iterator<String> it = tempBan.keys(); it.hasNext(); ) {
                String user = it.next();
                long unban_time = tempBan.getJSONObject(user).getLong("unban_time");
                long now = Instant.now().toEpochMilli();
                if (now < unban_time) {
                    ret.add(convertToLong(user));
                }
            }

            for (Object o : permBan) {
                ret.add(convertToLong(o));
            }
        }
        return (Long[]) ret.toArray();
    }

    public static boolean checkId(long id){
        Long[] blacklist = load();
        for(long check : blacklist){
            if(check == id){
                return true;
            }
        }
        return false;
    }

    public static long getBanLeft(long id) {
        JSONObject json = readJSONFromFile("/config/blacklist");
        JSONObject tempBan = json.getJSONObject("temp_ban");

        for (Iterator<String> it = tempBan.keys(); it.hasNext(); ) {
            long user = convertToLong(it.next());
            if(user == id){
                return tempBan.getJSONObject("" + user).getLong("unban_time") - Instant.now().toEpochMilli();
            }
        }
        //User is permbanned.
        return -1;
    }

    public static void unbanUser(long id){
        JSONObject json = readJSONFromFile("/config/blacklist");
        JSONObject tempBan = json.getJSONObject("temp_ban");
        JSONArray permBan = json.getJSONArray("perm_ban");
        JSONArray permOut = new JSONArray();
        try{
            tempBan.remove("" + id);
        }
        catch(Exception ignored){}
        for(Object permid : permBan){
            if((long)permid != id){
                permOut.put(id);
            }
        }
        json.remove("perm_ban");
        json.remove("temp_ban");
        json.append("perm_ban", permOut);
        json.append("temp_ban", tempBan);
        writeJSONToFile("/config/blacklist", json);
    }

    public static void banUser(long id){
        JSONObject json = readJSONFromFile("/config/blacklist");
        JSONArray perm_ban = json.getJSONArray("perm_ban");
        try{
            json.remove("perm_ban");
            perm_ban.put(id);
            json.put("perm_ban", perm_ban);
            writeJSONToFile("/config/blacklist", json);
        }
        catch(JSONException ignored){}
    }

    public static void banUser(long id, long duration){
        JSONObject json = readJSONFromFile("/config/blacklist");
        JSONObject temp_ban = json.getJSONObject("temp_ban");
        long now = Instant.now().toEpochMilli();
        try{
            temp_ban.remove("" + id); //Remove old tempban before setting new
        }
        catch(Exception ignored){}
        temp_ban.put("" + id, new JSONObject().put("ban_time", now).put("unban_time", now + duration));
        writeJSONToFile("/config/blacklist", json);
    }

    public static void banUser(long id, int days, int hours, int minutes, int seconds){
        long duration = 0;
        duration += days * (60 * 60 * 24);
        duration += hours * (60 * 60);
        duration += minutes * (60);
        duration += seconds;
        banUser(id, duration);
    }

    public static void banUser(long id, Calendar date){
        banUser(id, date.toInstant().toEpochMilli() - Instant.now().toEpochMilli());
    }

    public static String calculateTime(long seconds){
        int day = (int) TimeUnit.SECONDS.toDays(seconds);
        int hours = (int) TimeUnit.SECONDS.toHours(seconds) - day * 24;
        int minute = (int) ((int) TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.SECONDS.toHours(seconds) * 60);
        int second = (int) ((int) TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.SECONDS.toMinutes(seconds) * 60);
        return "$day day(s) $hours hour(s) $minute minute(s) and $second second(s)";
    }

    //INSERT Utils Code here. Self contain all methods.
    private static long convertToLong(Object any) {
        return Long.getLong(any.toString());
    }

    private static JSONObject readJSONFromFile(String path) {
        File file = new File(Paths.get("").toAbsolutePath().toString() + path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            writeJSONToFile(path, new JSONObject());
        }
        URL url = null;
        try {
            url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        JSONTokener tokener = null;
        try {
            tokener = new JSONTokener(url.openStream());
        }
        catch(IOException ignored){}

        if(tokener != null){
            return new JSONObject(tokener);
        }
        return null;
    }

    private static void writeJSONToFile(String path, JSONObject jsonObject){
        File file = new File(Paths.get("").toAbsolutePath().toString() + path);
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(jsonObject.toString());
            writer.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

}

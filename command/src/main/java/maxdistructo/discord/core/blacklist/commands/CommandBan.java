package maxdistructo.discord.core.blacklist.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import maxdistructo.discord.core.blacklist.Blacklist;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.GregorianCalendar;
import java.util.List;

public class CommandBan extends Command {
    public CommandBan(){
        this.help = "ban <@User> <Time(#d,#h,#m,#s)/Date(mm,dd,yyyy)> | Bans a user for the amount of time specified or until the date specified. If time is not specified, they are perm banned.";
        this.name = "ban";
        this.guildOnly = false;
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        //Prepare Interpretation of Ban Command
        String content = event.getMessage().getContentRaw();
        Member mentioned = getMentionedUser(event.getMessage());
        //command, mention, interpret
        String[] split = content.split(" ");
        if(split.length > 2) {
            if (split[2].contains("d") || split[2].contains("h") || split[2].contains("m") || split[2].contains("s")) {
                int days = Integer.parseInt(split[2].split("d")[0].replace(",", ""));
                int hours = Integer.parseInt(split[2].split("d")[1].split("h")[0].replace(",", ""));
                int minutes = Integer.parseInt(split[2].split("d")[1].split("h")[1].split("m")[0].replace(",", ""));
                int seconds = Integer.parseInt(split[2].split("d")[1].split("h")[1].split("m")[1].replace("s", "").replace(",", ""));
                Blacklist.banUser(mentioned.getIdLong(), days, hours, minutes, seconds);
            } else {
                int month = Integer.parseInt(split[2].split(",")[0]);
                int day = Integer.parseInt(split[2].split(",")[1]);
                int year = Integer.parseInt(split[2].split(",")[2]);
                Blacklist.banUser(mentioned.getIdLong(), new GregorianCalendar(year, month - 1, day));
            }
            event.reply("User "+mentioned.getAsMention()+" has been banned from using " + event.getJDA().getSelfUser().getAsMention());
        }

        else{
            Blacklist.banUser(mentioned.getIdLong());
            event.reply("User: "+mentioned.getAsMention()+" has been permanently banned from using " + event.getJDA().getSelfUser().getAsMention());
        }
    }
    
    private Member getMentionedUser(Message message){
        return getMentionedUser(message, 0);
    }
    
    private Member getMentionedUser(Message message, int mentionNum){
        List<Member> mentionedList = message.getMentionedMembers();
        Member mentioned;
        if(!mentionedList.isEmpty()){
            mentioned = mentionedList.get(mentionNum);
        }
        else{
            mentioned = null;
        }
        return mentioned;
    }
}

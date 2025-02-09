package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GroupHelpBot extends TelegramLongPollingBot {

    private final Set<String> spamKeywords = new HashSet<>();
    private static final long GROUP_ID = 5750963176L;
    private final Map<Long, Integer> userOffenseCount = new HashMap<>();
    private static final int MAX_OFFENSES = 10;

    public GroupHelpBot(String s) {

        spamKeywords.add("spam");
        spamKeywords.add("http://");
        spamKeywords.add("https://");
        spamKeywords.add("haqorat");
        spamKeywords.add("nojo'ya");
        spamKeywords.add("gandon");
        spamKeywords.add("yeban");
        spamKeywords.add("dalbayob");
        spamKeywords.add("jalab");
        spamKeywords.add("onangni skey");
        spamKeywords.add("qotaq");
        spamKeywords.add("kallanga qoatagi'm");
        spamKeywords.add("qiz sikdik");
        spamKeywords.add("og'zinga qo'tag'im");
        spamKeywords.add("qanjiq");
        spamKeywords.add("jalabcha");
        spamKeywords.add("kallanga ski");
        spamKeywords.add("kallanga skey");
        spamKeywords.add("KALLANGA SKEY");
        spamKeywords.add("dalbayobmisan");
        spamKeywords.add("Dalbayobmisan");
        spamKeywords.add("yebanmisan");
        spamKeywords.add("Yebanmisan");
        spamKeywords.add("dabba");
        spamKeywords.add("dabbamisan");
        spamKeywords.add("Dabbamisan");
        spamKeywords.add("og'zinga skey");
        spamKeywords.add("Og'zimga skey");
        spamKeywords.add("dalbayobsanda");
        spamKeywords.add("yebansanda");
        spamKeywords.add("jalabsanda");
        spamKeywords.add("pidarassanda");
        spamKeywords.add("xuyet qima");
        spamKeywords.add("xuyet qivossanmi");
        spamKeywords.add("sskib qo'yaman");
        spamKeywords.add("Kallanga qotag'm");
        spamKeywords.add("blyat");
        spamKeywords.add("kallanga skey gandon");
        spamKeywords.add("Kallanga sekey gandon");
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                String messageText = message.getText();
                int messageId = message.getMessageId();
                long chatId = message.getChatId();
                long userId = message.getFrom().getId();
                String userName = message.getFrom().getUserName();

                if (messageText.startsWith("/start")) {
                    sendTextMessage(GROUP_ID, "Bot ishga tushdi. Guruh boshqaruvchisi xizmatingizda!");
                    sendTextMessage(GROUP_ID, "Guruh ID'si: " + GROUP_ID);
                } else if (messageText.startsWith("/setrules")) {
                    sendTextMessage(GROUP_ID, "Guruh qoidalari: Hech qanday spam yoki nojo'ya xabarlar yubormang.");
                } else if (messageText.startsWith("/ban")) {
                    handleBanCommand(message);
                } else if (messageText.startsWith("/mute")) {
                    sendTextMessage(GROUP_ID, "⚠️ Ushbu foydalanuvchi vaqtincha sukutda.");
                } else if (containsSpam(messageText)) {
                    deleteMessage(chatId, messageId);
                    sendTextMessage(chatId, "⚠️ Diqqat! Nojo'ya xabar o'chirildi.");
                    trackUserOffenses(userId, userName, chatId);
                }
            }
        }
    }

    private void handleBanCommand(Message message) {
        String[] parts = message.getText().split(" ");
        if (parts.length == 2) {
            String userIdToBan = parts[1];
            try {
                BanChatMember banChatMember = new BanChatMember();
                banChatMember.setChatId(GROUP_ID);
                banChatMember.setUserId((long) Integer.parseInt(userIdToBan));
                execute(banChatMember);
                sendTextMessage(GROUP_ID, "🚫 Foydalanuvchi muvaffaqiyatli bloklandi.");
            } catch (Exception e) {
                sendTextMessage(GROUP_ID, "Xato: Foydalanuvchini bloklashda muammo yuz berdi.");
                e.printStackTrace();
            }
        } else {
            sendTextMessage(GROUP_ID, "Iltimos, /ban <foydalanuvchi_id> formatida yozing.");
        }
    }

    private boolean containsSpam(String text) {
        for (String keyword : spamKeywords) {
            if (text.toLowerCase().contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private void trackUserOffenses(long userId, String userName, long chatId) {
        int offenses = userOffenseCount.getOrDefault(userId, 0) + 1;
        userOffenseCount.put(userId, offenses);

        if (offenses >= MAX_OFFENSES) {
            banUser(userId, userName, chatId);
            userOffenseCount.remove(userId);
        }
    }

    private void banUser(long userId, String userName, long chatId) {
        try {
            BanChatMember banChatMember = new BanChatMember();
            banChatMember.setChatId(chatId);
            banChatMember.setUserId((long) userId);
            execute(banChatMember);
            sendTextMessage(chatId, "🚫 Foydalanuvchi " + (userName != null ? "@" + userName : "ID: " + userId) + " 10 ta yoki undan ortiq nojo'ya xabar sababli bloklandi.");
        } catch (TelegramApiException e) {
            sendTextMessage(chatId, "Xato: Foydalanuvchini bloklashda muammo yuz berdi.");
            e.printStackTrace();
        }
    }

    private void deleteMessage(long chatId, int messageId) {
        org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage deleteMessage = new org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage();
        deleteMessage.setChatId(String.valueOf(chatId));
        deleteMessage.setMessageId(messageId);
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            sendTextMessage(chatId, "Xato: Xabarni o'chirishda muammo yuz berdi.");
            System.err.println("Xabarni o'chirishda muammo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendTextMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Xabar yuborishda xatolik: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "t.me/gruppa_halper_bot. ";
    }

    @Override
    public String getBotToken() {
        return "7390535587:AAFfLjx5tsQffNTxQD8gZMNPu7MfbTw_9D0";
    }
}
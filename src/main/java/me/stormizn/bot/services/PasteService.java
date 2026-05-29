package me.stormizn.bot.services;

public interface PasteService {
    String upload(String content, String filename) throws Exception;
}
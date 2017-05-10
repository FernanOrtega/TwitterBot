package org.fogallego.twitterchatbot;

import twitter4j.*;

public class EntryPoint {

    public static void main(String[] args) throws TwitterException {
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        BotListener listener = new BotListener();
        twitterStream.addListener(listener);
        twitterStream.user();
    }

}

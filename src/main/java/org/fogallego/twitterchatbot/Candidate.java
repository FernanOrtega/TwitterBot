package org.fogallego.twitterchatbot;

import twitter4j.Status;

import java.util.List;

public class Candidate {

    private Status mainStatus;
    private List<Status> lstReply;
    private double simScore;

    public Candidate(Status mainStatus, List<Status> lstReply, double simScore) {
        this.mainStatus = mainStatus;
        this.lstReply = lstReply;
        this.simScore = simScore;
    }

    public Status getMainStatus() {
        return mainStatus;
    }

    public List<Status> getLstReply() {
        return lstReply;
    }

    public double getSimScore() {
        return simScore;
    }
}

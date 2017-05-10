package org.fogallego.twitterchatbot;

import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;
import twitter4j.*;

import java.util.*;
import java.util.stream.Collectors;

public class ResponseCreator {

    private StringMetric stringMetric;
    private Twitter twitter;
    private static final int COUNT = 100;
    private static final int MAX_NUM_PAGES = 1;
    private static final int MAX_RESULTS_SIZE = 1500;

    public ResponseCreator() throws TwitterException {
        stringMetric = StringMetrics.cosineSimilarity();
        twitter = TwitterFactory.getSingleton();
    }

    public String getReply(String message) throws TwitterException {
        String result;

        Candidate bestCandidate = getBestCandidate(message);
        result = createReplyText(bestCandidate);

        return result;
    }

    private Candidate getBestCandidate(String message) throws TwitterException {

        List<Candidate> lstCandidate = new ArrayList<>();
        List<Status> lstStatus = getStatuses(message);
        for (Status status : lstStatus) {
            List<Status> lstReply = getReplies(status);
            if (!lstReply.isEmpty()) {
                double simScore = getSimScore(message, status.getText());
                lstCandidate.add(new Candidate(status, lstReply, simScore));
            }
        }

        lstCandidate.sort(new Comparator<Candidate>() {
            @Override
            public int compare(Candidate o1, Candidate o2) {
                double sizeRepliesDiff = o1.getLstReply().size() - o2.getLstReply().size();
                double simDiff = o2.getSimScore() - o1.getSimScore();

                return (int) (sizeRepliesDiff + simDiff);
            }
        });

        return lstCandidate.size() > 0 ? lstCandidate.get(0) : null;
    }

    private List<Status> getStatuses(String keywords) throws TwitterException {

        List<Status> result = new ArrayList<>();
        long maxId = 0L;
        int numPage = 1;

        while (numPage <= MAX_NUM_PAGES && result.size() <= MAX_RESULTS_SIZE) {

            Query query = new Query(keywords);
            query.setMaxId(maxId);
            query.setCount(COUNT);
            QueryResult queryResult = twitter.search(query);
            result.addAll(queryResult.getTweets().stream().filter(status -> !status.isRetweet()).collect(Collectors.toList()));
            maxId = queryResult.getSinceId();
            numPage++;
        }

        return result;
    }

    private double getSimScore(String message, String text) {

        List<String> textSplitted = Arrays.asList(text.split("\\."));
        double simWhole = stringMetric.compare(message, text);
        OptionalDouble optionalDouble = textSplitted.stream().mapToDouble(split ->
                stringMetric.compare(message, split)).max();
        double simBestPart = optionalDouble.isPresent() ? optionalDouble.getAsDouble() : 0;

        return Math.max(simWhole, simBestPart);
    }

    private List<Status> getReplies(Status status) throws TwitterException {
        List<Status> replies;
        replies = new ArrayList<>();
        String screenName = status.getUser().getScreenName();
        List<Status> lstStatus = getStatuses("to:" + screenName);
        for (Status reply : lstStatus) {
            if (reply.getInReplyToStatusId() == status.getId()) {
                replies.add(reply);
            }
        }

        return replies;
    }

    private String createReplyText(Candidate bestCandidate) {

        String reply;

        if (bestCandidate == null) {
            reply = getRandomReply();
        } else {
            List<Status> lstReply = bestCandidate.getLstReply();
            // TODO: For now, the best reply is the first one
            Status bestReplyStatus = lstReply.get(0);
            reply = bestReplyStatus.getText();
        }

        return reply;
    }

    private String getRandomReply() {
        return "No tengo respuestas para ello...";
    }

}

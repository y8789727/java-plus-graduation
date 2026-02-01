package ru.practicum.ewm.client.stats;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RecommendationsClient {
    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client;

    public List<RecommendedEventProto> getRecommendationsForUser(Long userId, Long maxResults) {
        return Lists.newArrayList(client.getRecommendationsForUser(UserPredictionsRequestProto.newBuilder()
                        .setUserId(userId)
                        .setMaxResults(maxResults)
                        .build()));
    }

    public List<RecommendedEventProto> getSimilarEvents(Long userId, Long eventId, Long maxResults) {
        return Lists.newArrayList(client.getSimilarEvents(SimilarEventsRequestProto.newBuilder()
                        .setUserId(userId)
                        .setEventId(eventId)
                        .setMaxResults(maxResults)
                        .build()));
    }

    public List<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        return Lists.newArrayList(client.getInteractionsCount(InteractionsCountRequestProto.newBuilder()
                        .addAllEventId(eventIds)
                        .build()));
    }
}

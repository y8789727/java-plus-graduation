package ru.practicum.ewm.client.stats;

import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;

@Component
public class CollectorClient {
    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub client;

    public void sendUserAction(Long userId, Long eventId, ActionTypeProto actionType) {
        Instant time = Instant.now();
        client.collectUserAction(UserActionProto.newBuilder()
                        .setUserId(userId)
                        .setEventId(eventId)
                        .setActionType(actionType)
                        .setTimestamp(Timestamp.newBuilder().setSeconds(time.getEpochSecond()).setNanos(time.getNano()).build())
                        .build());
    }
}

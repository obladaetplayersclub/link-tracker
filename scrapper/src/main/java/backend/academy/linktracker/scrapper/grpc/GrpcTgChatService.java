package backend.academy.linktracker.scrapper.grpc;

import backend.academy.linktracker.scrapper.grpc.generated.ChatRequest;
import backend.academy.linktracker.scrapper.grpc.generated.ChatResponse;
import backend.academy.linktracker.scrapper.grpc.generated.TgChatServiceGrpc;
import backend.academy.linktracker.scrapper.service.ChatService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.transport", havingValue = "grpc")
public class GrpcTgChatService extends TgChatServiceGrpc.TgChatServiceImplBase {
    private final ChatService chatService;

    @Override
    public void registerChat(ChatRequest request, StreamObserver<ChatResponse> responseObserver) {
        log.atInfo().addKeyValue("chat_id", request.getId()).log("gRPC: Регистрация чата");
        chatService.register(request.getId());
        responseObserver.onNext(ChatResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteChat(ChatRequest request, StreamObserver<ChatResponse> responseObserver) {
        log.atInfo().addKeyValue("chat_id", request.getId()).log("gRPC: Удаление чата");
        chatService.delete(request.getId());
        responseObserver.onNext(ChatResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }
}

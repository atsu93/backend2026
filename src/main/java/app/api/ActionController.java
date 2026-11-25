package app.api;

import app.api.dto.TopUpRequest;
import app.api.dto.UpgradeRequest;
import app.api.dto.WatchRequest;
import app.service.StreamingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ActionController {

    private final StreamingService streamingService;

    public ActionController(StreamingService streamingService) {
        this.streamingService = streamingService;
    }

    @PostMapping("/watch")
    public Map<String, String> watch(@Valid @RequestBody WatchRequest request) {
        streamingService.watchMedia(request.profileId(), request.mediaId());
        return Map.of("status", "Successfully updated views");
    }

    @PostMapping("/upgrade")
    public Map<String, String> upgrade(@Valid @RequestBody UpgradeRequest request) {
        streamingService.buyPremium(request.profileId());
        return Map.of("status", "Premium status successfully purchased!");
    }

    @PostMapping("/topup")
    public Map<String, String> topUp(@Valid @RequestBody TopUpRequest request) {
        streamingService.topUpBalance(request.profileId(), request.amount());
        return Map.of("status", "Balance successfully topped up!");
    }
}

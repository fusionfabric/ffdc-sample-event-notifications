package com.trmsys.clock.clockapplication.controller;

import io.swagger.api.SampleApi;
import io.swagger.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class ClockApplicationController implements SampleApi {

	private static final String WEB_HOOK_ALLOWED_ORIGIN = "WebHook-Allowed-Origin";
	private static final String WEB_HOOK_FUSION_FABRIC_ORIGIN = "api.fusionfabric.cloud";
	private final SimpMessagingTemplate template;

	@Autowired
	ClockApplicationController(SimpMessagingTemplate template) {
		this.template = template;
	}

	@Override
	public ResponseEntity<Void> clock(Event event) {
		log.info("Event received {}", event.toString());
		// Send event to the endpoint
		this.template.convertAndSend("/clock/event",  event);
		// Return status code from the destination query
		return ResponseEntity.ok().build();
	}

	@Override
	public ResponseEntity<Void> validateEventEndpoint() {
		return ResponseEntity.ok()
				.header(WEB_HOOK_ALLOWED_ORIGIN, WEB_HOOK_FUSION_FABRIC_ORIGIN)
				.build();
	}
}

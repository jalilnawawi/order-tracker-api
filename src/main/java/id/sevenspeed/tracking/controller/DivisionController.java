package id.sevenspeed.tracking.controller;

import id.sevenspeed.tracking.dto.response.common.ApiResponse;
import id.sevenspeed.tracking.dto.response.division.DivisionResponse;
import id.sevenspeed.tracking.dto.response.division.QueueItemResponse;
import id.sevenspeed.tracking.service.DivisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/divisions")
@RequiredArgsConstructor
public class DivisionController {

    private final DivisionService divisionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DivisionResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(divisionService.findAll()));
    }

    @GetMapping("/{id}/queue")
    public ResponseEntity<ApiResponse<List<QueueItemResponse>>> getQueue(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
                divisionService.findQueueByDivisionId(id)));
    }
}
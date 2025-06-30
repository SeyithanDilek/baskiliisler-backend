package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.FactoryAssignDto;
import com.baskiliisler.backend.dto.FactoryRequestDto;
import com.baskiliisler.backend.dto.FactoryResponseDto;
import com.baskiliisler.backend.dto.OrderResponseDto;
import com.baskiliisler.backend.mapper.FactoryMapper;
import com.baskiliisler.backend.mapper.OrderMapper;
import com.baskiliisler.backend.model.Order;
import com.baskiliisler.backend.service.FactoryService;
import com.baskiliisler.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/factories")
public class FactoryController {
    private final OrderService orderService;
    private final FactoryService factoryService;

    @PatchMapping("/orders/{id}/assign-factory")
    public OrderResponseDto assignFactory(@PathVariable Long id,
                                          @RequestBody @Valid FactoryAssignDto dto) {
        Order o = orderService.assignFactory(id, dto.factoryId(), dto.deadline());
        return OrderMapper.toDto(o);
    }

    @PostMapping
    public ResponseEntity<FactoryResponseDto> create(@RequestBody @Valid FactoryRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FactoryMapper.toDto(factoryService.create(dto)));
    }

    @PutMapping(value = "/{id}")
    public FactoryResponseDto update(@PathVariable Long id,
                                     @RequestBody FactoryRequestDto dto) {
        return FactoryMapper.toDto(factoryService.update(id, dto));
    }

    @GetMapping
    public List<FactoryResponseDto> list(@RequestParam(defaultValue="false") boolean onlyActive) {
        return factoryService.list(onlyActive);
    }

    @GetMapping("/{id}")
    public FactoryResponseDto get(@PathVariable Long id) {
        return factoryService.get(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        factoryService.delete(id);
    }
}

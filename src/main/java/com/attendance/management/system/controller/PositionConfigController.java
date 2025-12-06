package com.attendance.management.system.controller;

import com.attendance.management.system.entity.PositionConfig;
import com.attendance.management.system.repository.PositionConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/positions")
@CrossOrigin(origins = "*")
public class PositionConfigController {

    private final PositionConfigRepository positionConfigRepository;

    public PositionConfigController(PositionConfigRepository positionConfigRepository) {
        this.positionConfigRepository = positionConfigRepository;
    }

    /**
     * 获取所有职务配置
     */
    @GetMapping
    public ResponseEntity<List<PositionConfig>> getAllPositions() {
        List<PositionConfig> positions = positionConfigRepository.findAll();
        return ResponseEntity.ok(positions);
    }

    /**
     * 根据职务ID获取配置
     */
    @GetMapping("/{positionId}")
    public ResponseEntity<?> getPositionById(@PathVariable String positionId) {
        Optional<PositionConfig> position = positionConfigRepository.findByPositionId(positionId);
        if (position.isPresent()) {
            return ResponseEntity.ok(position.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 创建职务配置
     */
    @PostMapping
    public ResponseEntity<PositionConfig> createPosition(@RequestBody PositionConfig positionConfig) {
        PositionConfig savedPosition = positionConfigRepository.save(positionConfig);
        return ResponseEntity.ok(savedPosition);
    }

    /**
     * 更新职务配置
     */
    @PutMapping("/{positionId}")
    public ResponseEntity<?> updatePosition(@PathVariable String positionId, @RequestBody PositionConfig positionConfig) {
        Optional<PositionConfig> existingPosition = positionConfigRepository.findByPositionId(positionId);
        if (existingPosition.isPresent()) {
            PositionConfig position = existingPosition.get();
            // 确保positionId匹配
            positionConfig.setPositionId(positionId);
            position.setPositionName(positionConfig.getPositionName());
            position.setWorkStartTime(positionConfig.getWorkStartTime());
            position.setWorkEndTime(positionConfig.getWorkEndTime());
            position.setRequiredWorkDays(positionConfig.getRequiredWorkDays());
            position.setRequiredDailyHours(positionConfig.getRequiredDailyHours());

            PositionConfig updatedPosition = positionConfigRepository.save(position);
            return ResponseEntity.ok(updatedPosition);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除职务配置
     */
    @DeleteMapping("/{positionId}")
    public ResponseEntity<Void> deletePosition(@PathVariable String positionId) {
        Optional<PositionConfig> existingPosition = positionConfigRepository.findByPositionId(positionId);
        if (existingPosition.isPresent()) {
            positionConfigRepository.delete(existingPosition.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}


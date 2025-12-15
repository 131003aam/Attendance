package com.attendance.management.system.dao;

import com.attendance.management.system.entity.PositionConfig;
import java.util.List;

public interface PositionConfigDAO {
    List<PositionConfig> findAll();
    PositionConfig findById(String pid);
    void insert(PositionConfig positionConfig);
    void update(PositionConfig positionConfig);
    void delete(String pid);
}




















